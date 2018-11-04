package by.andruhovich.server.service;

import by.andruhovich.server.command.CommandParser;
import by.andruhovich.server.console.ConsolePrinter;
import by.andruhovich.server.exception.file.FileActionTechnicalException;
import by.andruhovich.server.exception.file.FileNotFoundTechnicalException;
import by.andruhovich.server.exception.socket.CreateSocketTechnicalException;
import by.andruhovich.server.exception.socket.ReceiveDataTechnicalException;
import by.andruhovich.server.exception.socket.SendDataTechnicalException;
import by.andruhovich.server.file.FileReader;
import by.andruhovich.server.socket.UDPSocket;
import by.andruhovich.server.type.CommandType;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UDPSocketService {
    private UDPSocket udpSocket;
    private String dataFromClient;
    private String dataForClient;
    private int packetNumber;

    private FileReader fileReader;
    private int lastOffset = 0;
    private long packetCount;
    private String lastFilename;
    private boolean isDownloading = true;
    private long startDownloadTime;
    private int sentPacketCount = 0;
    private long sentBytesCount = 0;
    private boolean clientDisconnected = false;

    private static int CHECKING_PACKET_QUANTITY = 30;
    private Map<Integer, FileData> lastSentFilePackets;

    private static final int DATA_SIZE = 1024;

    public int serviceSocket() {
        int port = ConsolePrinter.enterPort();

        try {
            udpSocket = new UDPSocket(port);
        } catch (CreateSocketTechnicalException e) {
            System.out.println(e.getMessage());
            udpSocket.closeSocket();
            return -1;
        }

        while (true) {
            try {
                if (!clientDisconnected) {
                    dataFromClient = udpSocket.receiveData();
                }
                if (!udpSocket.isOldClient()) {
                    udpSocket.setNewClient();
                    clearFileInformation();
                }

                if (!udpSocket.isOldPort()) {
                    udpSocket.setNewPort();
                }
                clientDisconnected = false;

                CommandType commandType = CommandParser.getCommandType(dataFromClient);
                switch (commandType) {
                    case TIME:
                        timeCommand();
                        break;
                    case ECHO:
                        echoCommand();
                        break;
                    case DOWNLOAD:
                        if (isDownloading) {
                            continueDownloadCommand();
                            processSlidingWindow();
                            downloadCommand();
                            if (!clientDisconnected) {
                                endDownloadCommand();
                            }
                        } else {
                            if (startDownloadCommand()){
                                downloadCommand();
                                if (!clientDisconnected) {
                                    endDownloadCommand();
                                }
                            }
                        }
                        break;
                    case EXIT:
                        exitCommand();
                        break;
                    case UNDEFINED_COMMAND:
                    default:
                        undefinedCommand();
                }
            } catch (ReceiveDataTechnicalException | SendDataTechnicalException | FileActionTechnicalException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void timeCommand() throws SendDataTechnicalException {
        System.out.println("TIME command:)");
        Date time = new Date(System.currentTimeMillis());
        packetNumber++;
        dataForClient = packetNumber + " " + CommandType.TIME.name() + " " + time.toString();
        udpSocket.sendData(dataForClient.getBytes(), dataForClient.length());
    }

    private void echoCommand() throws SendDataTechnicalException {
        System.out.println("ECHO command:)");
        packetNumber++;
        String echoData = CommandParser.getData(dataFromClient);
        dataForClient = packetNumber + " " + CommandType.ECHO.name() + " " + echoData;
        udpSocket.sendData(dataForClient.getBytes(), dataForClient.length());
    }

    private void exitCommand() throws SendDataTechnicalException {
        System.out.println("EXIT command:(");
        packetNumber++;
        dataForClient = packetNumber + " " + CommandType.EXIT.name();
        udpSocket.sendData(dataForClient.getBytes(), dataForClient.length());
        packetNumber = 0;
    }

    private void undefinedCommand() throws SendDataTechnicalException {
        System.out.println("Undefined command:(");
        packetNumber++;
        dataForClient = packetNumber + " " + CommandType.UNDEFINED_COMMAND.name();
        udpSocket.sendData(dataForClient.getBytes(), dataForClient.length());
    }

    private boolean startDownloadCommand() throws FileActionTechnicalException, SendDataTechnicalException {
        System.out.println("START_DOWNLOAD command:)");
        clearFileInformation();
        lastFilename = CommandParser.getFilename(dataFromClient);
        fileReader = new FileReader();
        try {
            fileReader.openFile(lastFilename);
            packetCount = fileReader.calculatePacketCount(lastOffset, DATA_SIZE);
            isDownloading = true;
            packetNumber++;
            lastSentFilePackets = new LinkedHashMap<>();
            dataForClient = packetNumber + " " + CommandType.START_DOWNLOAD + " " + lastFilename + " " + packetCount;
            udpSocket.sendData(dataForClient.getBytes(), dataForClient.length());
            startDownloadTime = System.currentTimeMillis();
            TimeUnit.MILLISECONDS.sleep(100);
            return true;
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } catch (FileNotFoundTechnicalException e) {
            System.out.println(e.getMessage());
            packetNumber++;
            dataForClient = packetNumber + " " + CommandType.FILE_NOT_FOUND;
            udpSocket.sendData(dataForClient.getBytes(), dataForClient.length());
        }
        return false;
    }

    private void downloadCommand() throws FileActionTechnicalException, SendDataTechnicalException, ReceiveDataTechnicalException {
        System.out.println("Downloading file " + lastFilename);
        byte[] data = new byte[DATA_SIZE];
        while (sentPacketCount < packetCount && !clientDisconnected) {
            sentPacketCount++;
            int dataReadSize = fileReader.readDataFromFile(data, DATA_SIZE);
            byte[] filePacketForClient = createDownloadPacket(data, dataReadSize);
            udpSocket.sendData(filePacketForClient, filePacketForClient.length);
            sentBytesCount += dataReadSize;
            packetNumber++;
            lastOffset += dataReadSize;
            FileData currentPacket = new FileData(filePacketForClient, filePacketForClient.length);
            lastSentFilePackets.put(sentPacketCount, currentPacket);
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            if (lastSentFilePackets.size() == CHECKING_PACKET_QUANTITY) {
                processSlidingWindow();
            }
        }
    }

    private void continueDownloadCommand() throws SendDataTechnicalException {
        System.out.println("CONTINUE_DOWNLOAD command:)");
        startDownloadTime = System.currentTimeMillis();
        long lastPacketCount = packetCount - sentPacketCount + CHECKING_PACKET_QUANTITY;
        dataForClient = packetNumber + " " + CommandType.CONTINUE_DOWNLOAD + " " + lastFilename + " " + lastPacketCount;
        udpSocket.sendData(dataForClient.getBytes(), dataForClient.length());
        packetNumber++;
        sentBytesCount = 0;
        for (FileData currentData : lastSentFilePackets.values()) {
            udpSocket.sendData(currentData.getPacket(), currentData.getPacketSize());
            sentBytesCount += DATA_SIZE;
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void endDownloadCommand() throws SendDataTechnicalException {
        System.out.println("END_DOWNLOAD command:)");
        packetNumber++;
        float downloadSpeed = calculateDownloadSpeed();
        dataForClient = packetNumber + " " + CommandType.END_DOWNLOAD + " " + downloadSpeed;
        udpSocket.sendData(dataForClient.getBytes(), dataForClient.length());
        clearFileInformation();
        fileReader.closeFile();
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    private float calculateDownloadSpeed() {
        long currentTime = System.currentTimeMillis();
        float timeDifference = (float) (currentTime - startDownloadTime) / 1000;
        return (float) sentBytesCount / timeDifference;
    }

    private void resendPacketsCommand(int resendPacketCount) throws SendDataTechnicalException {
        System.out.println("RESEND_PACKETS command:)");
        System.out.println("Packets count: " + resendPacketCount);
        dataForClient = packetNumber + " " + CommandType.RESEND_PACKETS.name() + " " + resendPacketCount;
        udpSocket.sendData(dataForClient.getBytes(), dataForClient.length());
        packetNumber++;
    }

    private void noPacketsForResendCommand() throws SendDataTechnicalException {
        System.out.println("NO_PACKETS_FOR_RESEND command:)");
        dataForClient = packetNumber + " " + CommandType.NO_PACKETS_FOR_RESEND.name();
        udpSocket.sendData(dataForClient.getBytes(), dataForClient.length());
        packetNumber++;
        lastSentFilePackets.clear();
    }

    private void processSlidingWindow() throws ReceiveDataTechnicalException, SendDataTechnicalException {
        dataFromClient = udpSocket.receiveData();
        CommandType commandType = CommandParser.getCommandType(dataFromClient);
        switch (commandType) {
            case ACK:
                List<Integer> packetNumbers = CommandParser.getACKPacketNumbers(dataFromClient);
                int difference = lastSentFilePackets.size() - packetNumbers.size();
                if (difference != 0) {
                    resendPacketsCommand(difference);
                    resendPackets(packetNumbers);
                } else {
                    noPacketsForResendCommand();
                }
                lastSentFilePackets.clear();
                break;
            default:
                clientDisconnected = true;
                System.out.println("Client disconnected:(((((");
                break;
        }
    }

    private byte[] createDownloadPacket(byte[] data, int dataReadSize) {
        final int SPACE_COUNT = 3;
        byte[] result;
        byte[] sentPacketCountInBytes = Integer.valueOf(sentPacketCount).toString().getBytes();
        byte[] commandInBytes = CommandType.DOWNLOAD.name().getBytes();
        byte[] dataReadSizeInBytes = Integer.valueOf(dataReadSize).toString().getBytes();

        int resultSize = SPACE_COUNT + sentPacketCountInBytes.length + commandInBytes.length +
                dataReadSizeInBytes.length + dataReadSize;
        result = new byte[resultSize];

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

        return result;
    }

    private void resendPackets(List<Integer> packetNumbers) throws SendDataTechnicalException {
        /*for (Integer packetNumber : lastSentFilePackets.keySet()) {
            if (!packetNumbers.contains(packetNumber)) {
                Iterator<byte[]> iterator = lastSentFilePackets.get(packetNumber).keySet().iterator();
                byte[] data = iterator.next();
                int dataSize = lastSentFilePackets.get(packetNumber).get(data);
                byte[] fileDataForClient = createDownloadPacket(data, dataSize, sentPacketCount);
                udpSocket.sendData(fileDataForClient, dataSize);
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        }*/
    }

    private void clearFileInformation() {
        sentBytesCount = 0;
        sentPacketCount = 0;
        lastOffset = 0;
        isDownloading = false;
        packetCount = 0;
        startDownloadTime = 0;
        if (lastSentFilePackets != null) {
            lastSentFilePackets.clear();
        }
    }
}
