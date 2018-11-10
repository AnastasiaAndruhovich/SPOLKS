package by.andruhovich.client.service;

import by.andruhovich.client.command.CommandParser;
import by.andruhovich.client.command.TCPCommandAction;
import by.andruhovich.client.console.ConsolePrinter;
import by.andruhovich.client.console.ConsoleReader;
import by.andruhovich.client.exception.file.FileActionTechnicalException;
import by.andruhovich.client.exception.file.FileNotFoundTechnicalException;
import by.andruhovich.client.exception.socket.AttemptCreateSocketTechnicalException;
import by.andruhovich.client.exception.socket.ReceiveDataTechnicalException;
import by.andruhovich.client.file.FileWriter;
import by.andruhovich.client.socket.TCPSocket;
import by.andruhovich.client.type.CommandType;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

public class TCPSocketService {
    private static final int DATA_SIZE = 1500;

    private InetAddress serverIP;

    private TCPSocket tcpSocket;
    private String dataFromServer;
    private boolean isDownloading = false;
    private String filename;
    private FileWriter fileWriter;
    private int packetCount;

    public int serviceSocket() {
        serverIP = ConsolePrinter.enterIP();
        int port = ConsolePrinter.enterPort();

        try {
            tcpSocket = TCPCommandAction.connect(serverIP, port);
            fileWriter = new FileWriter();

            while (tcpSocket.isConnected()) {
                if (!isDownloading) {
                    System.out.println("Enter command, please");
                    TCPCommandAction.packetNumber++;
                    String dataForServer = TCPCommandAction.packetNumber + " " + ConsoleReader.getLine();
                    tcpSocket.sendData(dataForServer);
                }
                try {
                    byte[] data = new byte[DATA_SIZE];
                    tcpSocket.receiveByteData(data, DATA_SIZE);
                    dataFromServer = new String(data);
                    CommandType commandType = CommandParser.getCommandType(dataFromServer);
                    switch (commandType) {
                        case TIME:
                            timeCommand();
                            break;
                        case ECHO:
                            echoCommand();
                            break;
                        case START_DOWNLOAD:
                            startDownloadCommand();
                            downloadCommand();
                            break;
                        case CONTINUE_DOWNLOAD:
                            continueDownloadCommand();
                            break;
                        case NO_CONTINUE_DOWNLOAD:
                            isDownloading = false;
                            break;
                        case END_DOWNLOAD:
                            endDownloadCommand();
                            break;
                        case FILE_NOT_FOUND:
                            fileNotFoundCommand();
                            break;
                        case EXIT:
                            exitCommand();
                            return 0;
                        case UNDEFINED_COMMAND:
                        default:
                            undefinedCommand();
                    }
                } catch (ReceiveDataTechnicalException e) {
                    System.out.println(e.getMessage());
                    tcpSocket = TCPCommandAction.reconnect(tcpSocket);
                } catch (FileNotFoundTechnicalException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (AttemptCreateSocketTechnicalException e) {
            System.out.println(e.getMessage());
            return -1;
        }

        return 0;
    }

    private void timeCommand() {
        System.out.println("TIME command:)");
        dataFromServer = CommandParser.getData(dataFromServer);
        System.out.println("Current time: " + dataFromServer);
    }

    private void echoCommand() {
        System.out.println("ECHO command:)");
        dataFromServer = CommandParser.getData(dataFromServer);
        System.out.println("Data: " + dataFromServer);
    }

    private void startDownloadCommand() throws FileNotFoundTechnicalException {
        System.out.println("START_DOWNLOAD command:)");
        isDownloading = true;
        packetCount = CommandParser.getPacketCount(dataFromServer);
        System.out.println("Packet count: " + packetCount);
        filename = CommandParser.getFilename(dataFromServer);
        fileWriter.openFile(filename, true);

        int packetNumber = CommandParser.getPacketNumber(dataFromServer);
        String dataForServer = packetNumber + " " + CommandType.ACK;
        tcpSocket.sendData(dataForServer);
    }

    private void downloadCommand() throws ReceiveDataTechnicalException {
        byte[] data = new byte[DATA_SIZE];
        for (int i = 0; i < packetCount; i++) {
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tcpSocket.receiveFullyByteData(data, DATA_SIZE);
            dataFromServer = new String(data);
            int filePacketNumber = 0;
            try {
                filePacketNumber = CommandParser.getPacketNumber(dataFromServer);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            System.out.println(filePacketNumber + " packet");
            int dataSize = CommandParser.getFileDataSize(dataFromServer);
            byte[] fileDataFromServer = CommandParser.getFileData(data, dataSize);

            int packetNumber = CommandParser.getPacketNumber(dataFromServer);
            String dataForServer = packetNumber + " " + CommandType.ACK;
            tcpSocket.sendData(dataForServer);

            try {
                fileWriter.writeDataInFile(fileDataFromServer, dataSize);
            } catch (FileActionTechnicalException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void continueDownloadCommand() throws ReceiveDataTechnicalException, FileNotFoundTechnicalException {
        System.out.println("CONTINUE_DOWNLOAD command:)");
        isDownloading = true;
        filename = CommandParser.getFilename(dataFromServer);
        packetCount = CommandParser.getPacketCount(dataFromServer);
        System.out.println("Rest packet count: " + packetCount);
        fileWriter.openFile(filename, false);

        int packetNumber = CommandParser.getPacketNumber(dataFromServer);
        String dataForServer = packetNumber + " " + CommandType.ACK;
        tcpSocket.sendData(dataForServer);

        downloadCommand();
    }

    private void endDownloadCommand() {
        System.out.println("END_DOWNLOAD command:)");
        float downloadSpeed = CommandParser.getDownloadSpeed(dataFromServer);
        System.out.println("File has been downloaded! Speed " + downloadSpeed + " bytes/sec.");
        isDownloading = false;
        fileWriter.closeFile();
    }

    private void fileNotFoundCommand() {
        System.out.println("FILE_NOT_FOUND command:(");
        System.out.println("Try to change file name, please");
    }

    private void exitCommand() {
        System.out.println("EXIT command:(");
        tcpSocket.closeSocket();
    }

    private void undefinedCommand() {
        System.out.println("Undefined command:(");
    }
}
