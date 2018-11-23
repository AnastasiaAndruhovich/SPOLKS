package by.andruhovich.client.service;

import by.andruhovich.client.command.CommandParser;
import by.andruhovich.client.command.TCPCommandAction;
import by.andruhovich.client.console.ConsolePrinter;
import by.andruhovich.client.exception.socket.AttemptCreateSocketTechnicalException;
import by.andruhovich.client.exception.socket.ReceiveDataTechnicalException;
import by.andruhovich.client.socket.TCPSocket;
import by.andruhovich.client.type.CommandType;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

public class ManagerSocketService {
    private static final int DATA_SIZE = 1500;

    private InetAddress serverIP;

    private TCPSocket tcpSocket;
    private String dataFromServer;
    private String dataForServer;
    private int packetNumber;
    private int serverPort;

    private static final int MAX_ATTEMPT_COUNT = 10;
    private static final int SLEEPING_TIME_IN_MILLISECONDS = 10;
    private int attemptCount;

    public int serviceSocket() {
        serverIP = ConsolePrinter.enterIP();
        int port = ConsolePrinter.enterPort();
        packetNumber = 0;
        serverPort = 0;
        attemptCount = 0;

        try {
            tcpSocket = TCPCommandAction.connect(serverIP, port);

            while (tcpSocket.isConnected()) {
                try {
                    sendRequestGetPortNumber();
                    byte[] data = new byte[DATA_SIZE];
                    tcpSocket.receiveByteData(data, DATA_SIZE);
                    dataFromServer = new String(data);
                    CommandType commandType = CommandParser.getCommandType(dataFromServer);
                    switch (commandType) {
                        case GET_PORT_NUMBER:
                            getPortNumberCommand();
                            sendRequestExit();
                            break;
                        case ALL_PORTS_ARE_BUSY:
                            allPortsAreBusy();
                            break;
                        case EXIT:
                            exitCommand();
                            return serverPort;
                    }
                } catch (ReceiveDataTechnicalException e) {
                    System.out.println(e.getMessage());
                    tcpSocket = TCPCommandAction.reconnect(tcpSocket);
                }
            }
        } catch (AttemptCreateSocketTechnicalException e) {
            System.out.println(e.getMessage());
            return serverPort;
        }
        return serverPort;
    }

    private void sendRequestGetPortNumber() {
        packetNumber++;
        dataForServer = packetNumber + " " + CommandType.GET_PORT_NUMBER.name();
        tcpSocket.sendData(dataForServer);
    }

    private void sendRequestExit() {
        packetNumber++;
        dataForServer = packetNumber + " " + CommandType.EXIT.name();
        tcpSocket.sendData(dataForServer);
    }

    private void getPortNumberCommand() {
        System.out.println("GET_PORT_NUMBER command:)");
        serverPort = CommandParser.getPortNumber(dataFromServer);
        System.out.println("Server port is " + serverPort);
    }

    private void allPortsAreBusy() {
        if (attemptCount < MAX_ATTEMPT_COUNT) {
            try {
                TimeUnit.MILLISECONDS.sleep(SLEEPING_TIME_IN_MILLISECONDS);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            sendRequestGetPortNumber();
            attemptCount++;
        } else {
            System.out.println("Attempt count for getting server port is exceeded!");
            sendRequestExit();
        }
    }


    private void exitCommand() {
        System.out.println("EXIT command:(");
        tcpSocket.closeSocket();
    }

}
