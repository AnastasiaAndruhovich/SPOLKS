package by.andruhovich.server.service;

import by.andruhovich.server.command.CommandParser;
import by.andruhovich.server.command.TCPCommandAction;
import by.andruhovich.server.console.ConsolePrinter;
import by.andruhovich.server.console.ConsoleReader;
import by.andruhovich.server.data.ConfigFileData;
import by.andruhovich.server.exception.file.FileActionTechnicalException;
import by.andruhovich.server.exception.file.FileNotFoundTechnicalException;
import by.andruhovich.server.exception.socket.AcceptSocketTechnicalException;
import by.andruhovich.server.exception.socket.CreateSocketTechnicalException;
import by.andruhovich.server.exception.socket.ReceiveDataTechnicalException;
import by.andruhovich.server.exception.socket.SocketTimeoutTechnicalException;
import by.andruhovich.server.file.FileReader;
import by.andruhovich.server.socket.TCPSocket;
import by.andruhovich.server.type.CommandType;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.nio.channels.SelectionKey.*;

public class MultipleTCPSocketService {
    private String dataFromClient;
    private String dataForClient;
    private ByteBuffer byteBuffer;

    private HashMap<String, ConfigFileData> configFileDataMap;
    private String currentClientAddress;
    private SocketChannel currentSocketChannel;

    private static final int BUFFER_SIZE = 1500;
    private static final int DATA_SIZE = 1024;

    public int serviceSocket() {
        int port = ConsolePrinter.enterPort();
        configFileDataMap = new LinkedHashMap<>();
        byteBuffer = ByteBuffer.allocate( BUFFER_SIZE );

        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking( false );

            TCPSocket tcpSocket = new TCPSocket(port, serverSocketChannel);
            Selector selector = Selector.open();

            serverSocketChannel.register( selector, OP_ACCEPT);
            System.out.println( "Listening on port " + port);

            Set<SelectionKey> selectedKeys = null;
            SelectionKey key = null;

            while (true) {
                try {
                    int existingConnectionQuantity = selector.select();
                    if (existingConnectionQuantity == 0) {
                        continue;
                    }

                    selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                    while (keyIterator.hasNext()) {
                        key = keyIterator.next();
                        if(key.isAcceptable()) {
                            Socket clientSocket = tcpSocket.waitForClient();
                            SocketChannel clientSocketChannel = clientSocket.getChannel();
                            clientSocketChannel.configureBlocking( false );
                            clientSocketChannel.register(selector, SelectionKey.OP_READ );

                            String inetAddress = clientSocket.getInetAddress().getHostAddress();
                            if (configFileDataMap.containsKey(inetAddress)) {
                                System.out.println("Oy! It's our old client!");
                                configFileDataMap.get(inetAddress).setOldClient(true);
                            } else {
                                configFileDataMap.put(inetAddress, new ConfigFileData());
                            }

                        } else if (key.isReadable()) {
                            currentSocketChannel = (SocketChannel)key.channel();
                            ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                            byteBuffer.clear();
                            currentSocketChannel.read(byteBuffer);
                            byteBuffer.flip();
                            dataFromClient = new String(byteBuffer.array()).trim();

                            CommandType commandType = CommandParser.getCommandType(dataFromClient);
                            currentClientAddress = getCurrentClientAddress();
                            switch (commandType) {
                                case HANDSHAKE:
                                    handshakeCommand();
                                    break;
                                case TIME:
                                    timeCommand();
                                    break;
                                case ECHO:
                                    echoCommand();
                                    break;
                                case DOWNLOAD:
                                    if (configFileDataMap.get(currentClientAddress).isOldClient() && configFileDataMap.get(currentClientAddress).isDownloading()) {
                                        continueDownloadCommand();
                                    } else {
                                        startDownloadCommand();
                                    }
                                    break;
                                case ACK:
                                    if (!downloadCommand()) {
                                        endDownloadCommand();
                                    }
                                    break;
                                case EXIT:
                                    exitCommand();
                                    key.cancel();
                                    break;
                                default:
                                    defaultCommand();
                            }
                        }
                    }
                    selectedKeys.clear();

                } catch (AcceptSocketTechnicalException  | IOException | FileActionTechnicalException e) {
                    System.out.println(e.getMessage());
                    configFileDataMap.get(currentClientAddress).cleanHandshakeCount();
                    if (key != null) {
                        key.cancel();
                    }
                    if (selectedKeys != null) {
                        selectedKeys.clear();
                    }
                }
            }

        } catch (CreateSocketTechnicalException | IOException e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

    private void handshakeCommand() throws IOException {
        System.out.println("HANDSHAKE command:)");
        configFileDataMap.get(currentClientAddress).addHandshakeCount();
        if (configFileDataMap.get(currentClientAddress).getHandshakeCount() < 2) {
            configFileDataMap.get(currentClientAddress).addPacketNumber();
            dataForClient = configFileDataMap.get(currentClientAddress).getPacketNumber() + " " + CommandType.HANDSHAKE.name();
            byteBuffer.clear();
            byteBuffer.put(dataForClient.getBytes());
            byteBuffer.flip();
            currentSocketChannel.write(byteBuffer);
        }
    }

    private void timeCommand() throws IOException {
        System.out.println("TIME command:)");
        Date time = new Date(System.currentTimeMillis());
        configFileDataMap.get(currentClientAddress).addPacketNumber();
        dataForClient = configFileDataMap.get(currentClientAddress).getPacketNumber() + " " + CommandType.TIME.name() + " " + time.toString();
        byteBuffer.clear();
        byteBuffer.put(dataForClient.getBytes());
        byteBuffer.flip();
        currentSocketChannel.write(byteBuffer);
    }

    private void echoCommand() throws IOException {
        System.out.println("ECHO command:)");
        configFileDataMap.get(currentClientAddress).addPacketNumber();
        String echoData = CommandParser.getData(dataFromClient);
        dataForClient = configFileDataMap.get(currentClientAddress).getPacketNumber() + " " + CommandType.ECHO.name() + " " + echoData;
        byteBuffer.clear();
        byteBuffer.put(dataForClient.getBytes());
        byteBuffer.flip();
        currentSocketChannel.write(byteBuffer);
    }

    private void startDownloadCommand() throws FileActionTechnicalException, IOException {
        System.out.println("START_DOWNLOAD command:)");
        configFileDataMap.get(currentClientAddress).setLastFilename(CommandParser.getFilename(dataFromClient));
        try {
            configFileDataMap.get(currentClientAddress).openFile();
            configFileDataMap.get(currentClientAddress).calculatePacketCount(DATA_SIZE);
            configFileDataMap.get(currentClientAddress).setDownloading(true);
            configFileDataMap.get(currentClientAddress).addPacketNumber();
            dataForClient = configFileDataMap.get(currentClientAddress).getPacketNumber() + " " +
                    CommandType.START_DOWNLOAD + " " + configFileDataMap.get(currentClientAddress).getLastFilename() + " " +
                    configFileDataMap.get(currentClientAddress).getPacketCount();
            byteBuffer.clear();
            byteBuffer.put(dataForClient.getBytes());
            byteBuffer.flip();
            currentSocketChannel.write(byteBuffer);
            configFileDataMap.get(currentClientAddress).setStartDownloadTime();
        } catch (FileNotFoundTechnicalException e) {
            System.out.println(e.getMessage());
            configFileDataMap.get(currentClientAddress).addPacketNumber();
            dataForClient = configFileDataMap.get(currentClientAddress).getPacketNumber() + " " + CommandType.FILE_NOT_FOUND;
            byteBuffer.clear();
            byteBuffer.put(dataForClient.getBytes());
            byteBuffer.flip();
            currentSocketChannel.write(byteBuffer);
        }
    }

    private boolean downloadCommand() throws FileActionTechnicalException, IOException {
        if (configFileDataMap.get(currentClientAddress).isContinue()) {
            byteBuffer.clear();
            byteBuffer.put(configFileDataMap.get(currentClientAddress).getFileData().getPacket());
            byteBuffer.flip();
            currentSocketChannel.write(byteBuffer);
            configFileDataMap.get(currentClientAddress).setIsContinue(false);
            System.out.println(configFileDataMap.get(currentClientAddress).getSentPacketCount() + " packet");
            return true;
        }
        byte[] data = new byte[DATA_SIZE];
        if (configFileDataMap.get(currentClientAddress).isSentPacketCountLessThanPacketCount()) {
            int dataReadSize = configFileDataMap.get(currentClientAddress).readDataFromFile(data, DATA_SIZE);
            byte[] filePacketForClient = createDownloadPacket(data, dataReadSize);
            try {
                byteBuffer.clear();
                byteBuffer.put(filePacketForClient);
                byteBuffer.flip();
                currentSocketChannel.write(byteBuffer);
            } finally {
                configFileDataMap.get(currentClientAddress).setFileData(filePacketForClient, filePacketForClient.length);
            }
            configFileDataMap.get(currentClientAddress).addSentBytesCount(dataReadSize);
            configFileDataMap.get(currentClientAddress).addPacketNumber();
            configFileDataMap.get(currentClientAddress).addLastOffset(dataReadSize);
            configFileDataMap.get(currentClientAddress).addSentPacketCount();
            System.out.println(configFileDataMap.get(currentClientAddress).getSentPacketCount() + " packet");
            return true;
        }
        return false;
    }

    private void continueDownloadCommand() throws IOException {
        System.out.println("CONTINUE_DOWNLOAD command:)");
        configFileDataMap.get(currentClientAddress).addPacketNumber();
        long restPacketCount = configFileDataMap.get(currentClientAddress).getPacketCount() - configFileDataMap.get(currentClientAddress).getSentPacketCount() + 1;
        dataForClient = configFileDataMap.get(currentClientAddress).getPacketNumber() + " " + CommandType.CONTINUE_DOWNLOAD.name() + " " + configFileDataMap.get(currentClientAddress).getLastFilename() + " " + restPacketCount;
        byteBuffer.clear();
        byteBuffer.put(dataForClient.getBytes());
        byteBuffer.flip();
        currentSocketChannel.write(byteBuffer);
        configFileDataMap.get(currentClientAddress).setStartDownloadTime();
        configFileDataMap.get(currentClientAddress).cleanSentBytesCount();

        configFileDataMap.get(currentClientAddress).setIsContinue(true);
    }

    private void endDownloadCommand() throws IOException {
        System.out.println("END_DOWNLOAD command:)");
        configFileDataMap.get(currentClientAddress).addPacketNumber();
        float downloadSpeed = configFileDataMap.get(currentClientAddress).calculateDownloadSpeed();
        dataForClient = configFileDataMap.get(currentClientAddress).getPacketNumber() + " " + CommandType.END_DOWNLOAD + " " + downloadSpeed;
        byteBuffer.clear();
        byteBuffer.put(dataForClient.getBytes());
        byteBuffer.flip();
        currentSocketChannel.write(byteBuffer);
        configFileDataMap.get(currentClientAddress).clean();
    }

    private void exitCommand() throws IOException {
        System.out.println("EXIT command:( address: " + currentClientAddress);
        configFileDataMap.get(currentClientAddress).addPacketNumber();
        dataForClient = configFileDataMap.get(currentClientAddress).getPacketNumber() + " " + CommandType.EXIT.name();
        byteBuffer.clear();
        byteBuffer.put(dataForClient.getBytes());
        byteBuffer.flip();
        currentSocketChannel.write(byteBuffer);
        configFileDataMap.remove(currentClientAddress);
    }

    private void defaultCommand() throws IOException {
        System.out.println("Undefined command:(");
        configFileDataMap.get(currentClientAddress).addPacketNumber();
        dataForClient = configFileDataMap.get(currentClientAddress).getPacketNumber() + " " + CommandType.UNDEFINED_COMMAND.name();
        byteBuffer.clear();
        byteBuffer.put(dataForClient.getBytes());
        byteBuffer.flip();
        currentSocketChannel.write(byteBuffer);
    }

    private String getCurrentClientAddress() throws IOException {
        String address = currentSocketChannel.getRemoteAddress().toString();
        address = address.split(":")[0];
        return address.replaceAll("/", "");
    }

    private byte[] createDownloadPacket(byte[] data, int dataReadSize) {
        byte[] result;
        byte[] sentPacketCountInBytes = Integer.valueOf(configFileDataMap.get(currentClientAddress).getSentPacketCount() + 1).toString().getBytes();
        byte[] commandInBytes = CommandType.DOWNLOAD.name().getBytes();
        byte[] dataReadSizeInBytes = Integer.valueOf(dataReadSize).toString().getBytes();

        result = new byte[BUFFER_SIZE];

        int currentPosition;
        for (currentPosition = 0; currentPosition < sentPacketCountInBytes.length; currentPosition++) {
            result[currentPosition] = sentPacketCountInBytes[currentPosition];
        }
        result[currentPosition] = ' ';
        currentPosition++;

        for (int i = 0; i < commandInBytes.length; i++, currentPosition++) {
            result[currentPosition] = commandInBytes[i];
        }
        result[currentPosition] = ' ';
        currentPosition++;

        for (int i = 0; i < dataReadSizeInBytes.length; i++, currentPosition++) {
            result[currentPosition] = dataReadSizeInBytes[i];
        }
        result[currentPosition] = ' ';
        currentPosition++;

        for (int i = 0; i < dataReadSize; i++, currentPosition++) {
            result[currentPosition] = data[i];
        }

        for (int i = currentPosition; i < BUFFER_SIZE; i++) {
            result[i] = 0;
        }

        return result;
    }
}
