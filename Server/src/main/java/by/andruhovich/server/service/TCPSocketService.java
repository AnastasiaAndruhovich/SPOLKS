package by.andruhovich.server.service;

import by.andruhovich.server.command.CommandParser;
import by.andruhovich.server.command.TCPCommandAction;
import by.andruhovich.server.console.ConsolePrinter;
import by.andruhovich.server.console.ConsoleReader;
import by.andruhovich.server.exception.file.FileActionTechnicalException;
import by.andruhovich.server.exception.file.FileNotFoundTechnicalException;
import by.andruhovich.server.exception.socket.*;
import by.andruhovich.server.file.FileReader;
import by.andruhovich.server.socket.TCPSocket;
import by.andruhovich.server.type.CommandType;

import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TCPSocketService {
    private static final int DATA_SIZE = 1024;

    private boolean isDownloading;
    private String lastFilename;
    private int lastOffset = 0;
    private InetAddress oldClient = null;
    private Socket clientSocket = null;
    private int packetNumber;
    private String dataFromClient;
    private String dataForClient;
    private int handshakeCount = 0;
    private TCPSocket tcpSocket;
    private FileReader fileReader;
    private long packetCount;
    private int sentPacketCount = 0;
    private byte[] lastSentData;
    private int lastSendDataSize;
    private long startDownloadTime;
    private long sentBytesCount = 0;

    public int serviceSocket() {
        int port = ConsolePrinter.enterPort();

        try {
            tcpSocket = new TCPSocket(port);
        } catch (CreateSocketTechnicalException e) {
            System.out.println(e.getMessage());
            ConsoleReader.close();
            return -1;
        }

        while (true) {
            boolean firstChecking;
            try {
                clientSocket = tcpSocket.waitForClient();
                firstChecking = true;
                if (clientSocket.getInetAddress().equals(oldClient)) {
                    System.out.println("It's our old client!");
                }

                while (tcpSocket.isHaveClient()) {
                    if (handshakeCount == 2 && firstChecking) {
                        if (isDownloading) {
                            continueDownloadCommand();
                            endDownloadCommand();
                        } else {
                            noContinueDownloadCommand();
                        }
                        firstChecking = false;
                    }

                    dataFromClient = tcpSocket.receiveData();
                    CommandType commandType = CommandParser.getCommandType(dataFromClient);
                    packetNumber = CommandParser.getPacketNumber(dataFromClient);
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
                            startDownloadCommand();
                            downloadCommand();
                            endDownloadCommand();
                            break;
                        case EXIT:
                            exitCommand();
                            break;
                        default:
                            defaultCommand();
                    }
                }
                if (!ConsolePrinter.isServerWantToContinue()) {
                    tcpSocket.closeSocket();
                    ConsoleReader.close();
                    break;
                } else {
                    oldClient = clientSocket.getInetAddress();
                    handshakeCount = 0;
                    fileReader.closeFile();                }
            } catch (AcceptSocketTechnicalException | ReceiveDataTechnicalException | SocketTimeoutTechnicalException |
                    FileActionTechnicalException e) {
                System.out.println(e.getMessage());
                tcpSocket.setNoClient();
                handshakeCount = 0;
                if (clientSocket != null) {
                    oldClient = clientSocket.getInetAddress();
                }
            }
        }
        return 0;
    }

    private void handshakeCommand() throws SocketTimeoutTechnicalException {
        System.out.println("HANDSHAKE command:)");
        handshakeCount++;
        if (handshakeCount < 2) {
            packetNumber++;
            dataForClient = packetNumber + " " + CommandType.HANDSHAKE.name();
            TCPCommandAction.sendData(tcpSocket, clientSocket, dataForClient);
        }
    }

    private void timeCommand() throws SocketTimeoutTechnicalException {
        System.out.println("TIME command:)");
        Date time = new Date(System.currentTimeMillis());
        packetNumber++;
        dataForClient = packetNumber + " " + CommandType.TIME.name() + " " + time.toString();
        TCPCommandAction.sendData(tcpSocket, clientSocket, dataForClient);
    }

    private void echoCommand() throws SocketTimeoutTechnicalException {
        System.out.println("ECHO command:)");
        packetNumber++;
        String echoData = CommandParser.getData(dataFromClient);
        dataForClient = packetNumber + " " + CommandType.ECHO.name() + " " + echoData;
        TCPCommandAction.sendData(tcpSocket, clientSocket, dataForClient);
    }

    private void exitCommand() throws SocketTimeoutTechnicalException {
        System.out.println("EXIT command:(");
        packetNumber++;
        dataForClient = packetNumber + " " + CommandType.EXIT.name();
        TCPCommandAction.sendData(tcpSocket, clientSocket, dataForClient);
        tcpSocket.setNoClient();
    }

    private void defaultCommand() throws SocketTimeoutTechnicalException {
        System.out.println("Undefined command:(");
        packetNumber++;
        dataForClient = packetNumber + " " + CommandType.UNDEFINED_COMMAND.name();
        TCPCommandAction.sendData(tcpSocket, clientSocket, dataForClient);
    }

    private void startDownloadCommand() throws SocketTimeoutTechnicalException, FileActionTechnicalException {
        System.out.println("START_DOWNLOAD command:)");
        lastFilename = CommandParser.getFilename(dataFromClient);
        fileReader = new FileReader();
        try {
            fileReader.openFile(lastFilename);
            packetCount = fileReader.calculatePacketCount(lastOffset, DATA_SIZE);
            isDownloading = true;
            packetNumber++;
            dataForClient = packetNumber + " " + CommandType.START_DOWNLOAD + " " + lastFilename + " " + packetCount;
            TCPCommandAction.sendData(tcpSocket, clientSocket, dataForClient);
            startDownloadTime = System.currentTimeMillis();
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } catch (FileNotFoundTechnicalException e) {
            System.out.println(e.getMessage());
            packetNumber++;
            dataForClient = packetNumber + " " + CommandType.FILE_NOT_FOUND;
            TCPCommandAction.sendData(tcpSocket, clientSocket, dataForClient);
        }
    }

    private void downloadCommand() throws FileActionTechnicalException, SocketTimeoutTechnicalException, ReceiveDataTechnicalException {
        System.out.println("Downloading file " + lastFilename);
        byte[] data = new byte[DATA_SIZE];
        while (sentPacketCount < packetCount) {
            int dataReadSize = fileReader.readDataFromFile(data, DATA_SIZE);
            try {
                byte[] filePacketForClient = createDownloadPacket(data, dataReadSize);
                TCPCommandAction.sendData(tcpSocket, clientSocket, filePacketForClient, filePacketForClient.length);
                dataFromClient = tcpSocket.receiveData();
                CommandType commandType = CommandParser.getCommandType(dataFromClient);
                switch (commandType) {
                    case ACK:
                        System.out.println("ACK command:)");
                        break;
                    default:
                        break;
                }
            } catch (SocketTimeoutTechnicalException | ReceiveDataTechnicalException e) {
                lastSentData = data;
                lastSendDataSize = dataReadSize;
                throw e;
            }
            sentBytesCount += dataReadSize;
            packetNumber++;
            lastOffset += dataReadSize;
            sentPacketCount++;
            System.out.println(sentPacketCount + " packet");
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void continueDownloadCommand() throws SocketTimeoutTechnicalException, FileActionTechnicalException, ReceiveDataTechnicalException {
        System.out.println("CONTINUE_DOWNLOAD command:)");
        packetNumber++;
        long restPacketCount = packetCount - sentPacketCount;
        dataForClient = packetNumber + " " + CommandType.CONTINUE_DOWNLOAD.name() + " " + lastFilename + " " + restPacketCount;
        TCPCommandAction.sendData(tcpSocket, clientSocket, dataForClient);
        startDownloadTime = System.currentTimeMillis();
        sentBytesCount = 0;
        try {
            TimeUnit.MILLISECONDS.sleep(100);
            byte[] filePacketForClient = createDownloadPacket(lastSentData, lastSendDataSize);
            TCPCommandAction.sendData(tcpSocket, clientSocket, filePacketForClient, filePacketForClient.length);

            dataFromClient = tcpSocket.receiveData();
            CommandType commandType = CommandParser.getCommandType(dataFromClient);
            switch (commandType) {
                case ACK:
                    System.out.println("ACK command:)");
                    break;
                default:
                    break;
            }

            packetNumber++;
            sentPacketCount++;
            sentBytesCount += lastSendDataSize;
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        downloadCommand();
    }

    private void noContinueDownloadCommand() throws SocketTimeoutTechnicalException {
        System.out.println("NO_CONTINUE_DOWNLOAD command:)");
        packetNumber++;
        dataForClient = packetNumber + " " + CommandType.NO_CONTINUE_DOWNLOAD.name();
        TCPCommandAction.sendData(tcpSocket, clientSocket, dataForClient);
    }

    private void endDownloadCommand() throws SocketTimeoutTechnicalException {
        System.out.println("END_DOWNLOAD command:)");
        packetNumber++;
        float downloadSpeed = calculateDownloadSpeed();
        dataForClient = packetNumber + " " + CommandType.END_DOWNLOAD + " " + downloadSpeed;
        TCPCommandAction.sendData(tcpSocket, clientSocket, dataForClient);
        isDownloading = false;
        lastOffset = 0;
        sentPacketCount = 0;
        packetCount = 0;
        fileReader.closeFile();
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    private float calculateDownloadSpeed() {
        long currentTime = System.currentTimeMillis();
        float timeDifference = (float)(currentTime - startDownloadTime) / 1000;
        return ((float)sentBytesCount / timeDifference);
    }

    private byte[] createDownloadPacket(byte[] data, int dataReadSize) {
        byte[] result;
        byte[] sentPacketCountInBytes = Integer.valueOf(sentPacketCount + 1).toString().getBytes();
        byte[] commandInBytes = CommandType.DOWNLOAD.name().getBytes();
        byte[] dataReadSizeInBytes = Integer.valueOf(dataReadSize).toString().getBytes();

        result = new byte[tcpSocket.getBufferSize()];

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

        for (int i = currentPosition; i < tcpSocket.getBufferSize(); i++) {
            result[i] = 0;
        }

        return result;
    }
}
