package by.andruhovich.client.service;

import by.andruhovich.client.command.CommandParser;
import by.andruhovich.client.console.ConsolePrinter;
import by.andruhovich.client.console.ConsoleReader;
import by.andruhovich.client.exception.file.FileActionTechnicalException;
import by.andruhovich.client.exception.file.FileNotFoundTechnicalException;
import by.andruhovich.client.exception.socket.CreateSocketTechnicalException;
import by.andruhovich.client.exception.socket.ReceiveDataTechnicalException;
import by.andruhovich.client.exception.socket.SendDataTechnicalException;
import by.andruhovich.client.file.FileWriter;
import by.andruhovich.client.socket.UDPSocket;
import by.andruhovich.client.type.CommandType;

import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;

public class UDPSocketService {
    private UDPSocket udpSocket;
    private InetAddress serverIP;
    private String dataFromServer;
    private byte[] fileDataFromServer;
    private byte[] dataForServer;
    private int packetNumber = 0;

    private FileWriter fileWriter;
    private boolean isDownloading;
    private int packetCount;
    private String filename;

    private static int CHECKING_PACKET_QUANTITY = 30;
    private Map<Integer, FileData> receivedFileData;

    public int serviceSocket() {
        serverIP = ConsolePrinter.enterIP();
        int port = ConsolePrinter.enterPort();

        try {
            udpSocket = new UDPSocket(serverIP, port);
            udpSocket.setServer(serverIP, port);
            while (true) {
                if (!isDownloading) {
                    System.out.println("Enter command, please");
                    packetNumber++;
                    dataForServer = (packetNumber + " " + ConsoleReader.getLine()).getBytes();
                    udpSocket.sendData(dataForServer, dataForServer.length);
                }
                dataFromServer = udpSocket.receiveStringData();

                CommandType commandType = CommandParser.getCommandType(dataFromServer);
                switch (commandType) {
                    case TIME:
                        System.out.println("TIME command:)");
                        dataFromServer = CommandParser.getData(dataFromServer);
                        System.out.println("Current time: " + dataFromServer);
                        break;
                    case ECHO:
                        System.out.println("ECHO command:)");
                        dataFromServer = CommandParser.getData(dataFromServer);
                        System.out.println("Data: " + dataFromServer);
                        break;
                    case START_DOWNLOAD:
                        startDownloadCommand();
                        downloadCommand();
                        break;
                    case CONTINUE_DOWNLOAD:
                        continueDownloadCommand();
                        downloadCommand();
                        break;
                    case FILE_NOT_FOUND:
                        fileNotFoundCommand();
                        break;
                    case END_DOWNLOAD:
                        endDownloadCommand();
                        break;
                    case EXIT:
                        System.out.println("EXIT command:(");
                        udpSocket.closeSocket();
                        return 0;
                    case UNDEFINED_COMMAND:
                    default:
                        System.out.println("Undefined command:(");
                }
            }
        } catch (CreateSocketTechnicalException e) {
            e.printStackTrace();
        } catch (SendDataTechnicalException e) {
            e.printStackTrace();
        } catch (ReceiveDataTechnicalException e) {
            e.printStackTrace();
        } catch (FileNotFoundTechnicalException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void startDownloadCommand() throws FileNotFoundTechnicalException {
        System.out.println("START_DOWNLOAD command:)");
        isDownloading = true;
        packetCount = CommandParser.getPacketCount(dataFromServer);
        System.out.println("Packet count: " + packetCount);
        filename = CommandParser.getFilename(dataFromServer);
        receivedFileData = new LinkedHashMap<>();
        fileWriter = new FileWriter();
        fileWriter.openFile(filename, true);
    }

    private void continueDownloadCommand() throws FileNotFoundTechnicalException {
        System.out.println("CONTINUE_DOWNLOAD command:)");
        isDownloading = true;
        filename = CommandParser.getFilename(dataFromServer);
        packetCount = CommandParser.getPacketCount(dataFromServer);
        System.out.println("Packet count: " + packetCount);
        filename = CommandParser.getFilename(dataFromServer);
        receivedFileData = new LinkedHashMap<>();
        fileWriter = new FileWriter();
        fileWriter.openFile(filename, false);
    }

    private void ackCommand() throws SendDataTechnicalException {
        dataForServer = (packetNumber + " " + CommandType.ACK + " " + parsePacketNumbers()).getBytes();
        udpSocket.sendData(dataForServer, dataForServer.length);
    }

    private void downloadCommand() throws ReceiveDataTechnicalException, SendDataTechnicalException {
        for (int i = 0; i < packetCount; i++) {
            fileDataFromServer = udpSocket.receiveByteData();
            dataFromServer = new String(fileDataFromServer);
            int filePacketNumber = CommandParser.getPacketNumber(dataFromServer);
            System.out.println(filePacketNumber + "packet");
            int dataSize = CommandParser.getFileDataSize(dataFromServer);
            fileDataFromServer = CommandParser.getFileData(fileDataFromServer, dataSize);
            FileData currentData = new FileData(fileDataFromServer, dataSize);
            receivedFileData.put(filePacketNumber, currentData);
            if (receivedFileData.size() == CHECKING_PACKET_QUANTITY) {
                ackCommand();
                dataFromServer = udpSocket.receiveStringData();
                CommandType commandType = CommandParser.getCommandType(dataFromServer);
                switch (commandType) {
                    case RESEND_PACKETS:
                        System.out.println("RESEND_PACKET command:(");
                        break;
                    case NO_PACKETS_FOR_RESEND:
                        System.out.println("NO_PACKETS_FOR_RESEND command:)");
                        writeDataInFile();
                        receivedFileData.clear();
                        break;
                }
            }
        }
        writeDataInFile();
        receivedFileData.clear();
    }

    private void fileNotFoundCommand() {
        System.out.println("FILE_NOT_FOUND command:(");
        System.out.println("Try to change file name, please");
    }

    private void endDownloadCommand() {
        System.out.println("END_DOWNLOAD command:)");
        float downloadSpeed = CommandParser.getDownloadSpeed(dataFromServer);
        System.out.println("File has been downloaded! Speed " + downloadSpeed + " bytes/sec.");
        isDownloading = false;
        receivedFileData.clear();
        fileWriter.closeFile();
    }

    private String parsePacketNumbers() {
        StringBuilder result = new StringBuilder();
        for (Integer packetNumber : receivedFileData.keySet()) {
            result.append(packetNumber);
            result.append(" ");
        }
        result.deleteCharAt(result.lastIndexOf(" "));
        return result.toString();
    }

    private void writeDataInFile() {
        for (FileData currentData : receivedFileData.values()) {
            try {
                fileWriter.writeDataInFile(currentData.getPacket(), currentData.getPacketSize());
            } catch (FileActionTechnicalException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
