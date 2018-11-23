package by.andruhovich.manager.service;

import by.andruhovich.manager.command.CommandParser;
import by.andruhovich.manager.console.ConsolePrinter;
import by.andruhovich.manager.exception.AcceptSocketTechnicalException;
import by.andruhovich.manager.exception.CreateSocketTechnicalException;
import by.andruhovich.manager.socket.TCPSocket;
import by.andruhovich.manager.type.CommandType;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

import static java.nio.channels.SelectionKey.OP_ACCEPT;

public class ManagerService {
    private String dataFromClient;
    private String dataForClient;
    private ByteBuffer byteBuffer;

    private String currentClientAddress;
    private SocketChannel currentSocketChannel;

    private static final int BUFFER_SIZE = 1500;
    private static final int DATA_SIZE = 1024;

    private HashMap<Integer, String> portList;
    private static final int START_PORT = 5000;

    private HashMap<String, Process> processList;
    private static final int MAX_PROCESS_QUANTITY = 10;

    private int packetNumber;

    public int serviceManager() {
        int port = ConsolePrinter.enterPort();
        byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        fullPortList();
        packetNumber = 0;
        processList = new HashMap<>();

        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);

            TCPSocket tcpSocket = new TCPSocket(port, serverSocketChannel);
            Selector selector = Selector.open();

            serverSocketChannel.register(selector, OP_ACCEPT);
            System.out.println("Listening on port " + port);

            SelectionKey key = null;

            while (true) {
                try {
                    int existingConnectionQuantity = selector.select();
                    if (existingConnectionQuantity == 0) {
                        continue;
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    return -1;
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                while (keyIterator.hasNext()) {
                    try {
                        key = keyIterator.next();
                        if (key.isAcceptable()) {
                            Socket clientSocket = tcpSocket.waitForClient();
                            SocketChannel clientSocketChannel = clientSocket.getChannel();
                            clientSocketChannel.configureBlocking(false);
                            clientSocketChannel.register(selector, SelectionKey.OP_READ);

                        } else if (key.isReadable()) {
                            currentSocketChannel = (SocketChannel) key.channel();
                            ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                            byteBuffer.clear();
                            currentSocketChannel.read(byteBuffer);
                            byteBuffer.flip();
                            dataFromClient = new String(byteBuffer.array()).trim();

                            CommandType commandType = CommandParser.getCommandType(dataFromClient);
                            currentClientAddress = getCurrentClientAddress();
                            switch (commandType) {
                                case GET_PORT_NUMBER:
                                    removeDeadProcesses();
                                    getPortNumberCommand();
                                    break;
                                case EXIT:
                                    exitCommand();
                                    createProcess();
                                    key.cancel();
                                    break;
                            }
                        }
                    } catch (AcceptSocketTechnicalException | IOException e) {
                        System.out.println(e.getMessage());
                        key.cancel();
                    }

                }
                selectedKeys.clear();
            }
        } catch (CreateSocketTechnicalException | IOException e) {
            System.out.println(e.getMessage());
            return -1;
        }

    }

    private void getPortNumberCommand() throws IOException {
        System.out.println("GET_PORT_NUMBER command:) " + currentClientAddress);
        int port = getFirstFreePort();
        packetNumber++;
        if (port != 0) {
            dataForClient = packetNumber + " " + CommandType.GET_PORT_NUMBER.name() + " " + port;
        } else {
            dataForClient = packetNumber + " " + CommandType.ALL_PORTS_ARE_BUSY.name();
        }
        byteBuffer.clear();
        byteBuffer.put(dataForClient.getBytes());
        byteBuffer.flip();
        currentSocketChannel.write(byteBuffer);
    }

    private void exitCommand() throws IOException {
        System.out.println("EXIT command:( address: " + currentClientAddress);
        packetNumber++;
        dataForClient = packetNumber + " " + CommandType.EXIT.name();
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

    private void fullPortList() {
        portList = new HashMap<>();
        for (int i = 0; i < MAX_PROCESS_QUANTITY; i++) {
            portList.put(START_PORT + i, "0");
        }
    }

    private Integer getFirstFreePort() {
        for (Map.Entry<Integer, String> pair : portList.entrySet()) {
            if (pair.getValue().equals("0")) {
                pair.setValue(currentClientAddress);
                return pair.getKey();
            }
        }
        return 0;
    }

    private Integer getPortByAddress(String address) {
        for (Map.Entry<Integer, String> pair : portList.entrySet()) {
            if (pair.getValue().equals(address)) {
                return pair.getKey();
            }
        }
        return 0;
    }

    private void returnPort(String clientAddress) {
        for (Map.Entry<Integer, String> pair : portList.entrySet()) {
            if (pair.getValue().equals(clientAddress)) {
                pair.setValue("0");
                break;
            }
        }
    }

    private void removeDeadProcesses() {
        Iterator<Map.Entry<String, Process>> iterator = processList.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Process> pair = iterator.next();
            if (!pair.getValue().isAlive()) {
                returnPort(pair.getKey());
                processList.remove(pair.getKey());
            }
        }
    }

    private void createProcess() throws IOException {
        int port = getPortByAddress(currentClientAddress);
        ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "start", "java", "-jar", "Server.jar", String.valueOf(port));
        Process process = processBuilder.start();
        processList.put(currentClientAddress, process);
    }
}
