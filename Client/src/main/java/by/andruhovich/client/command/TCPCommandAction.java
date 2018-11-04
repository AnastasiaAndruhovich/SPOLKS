package by.andruhovich.client.command;

import by.andruhovich.client.exception.socket.AttemptCreateSocketTechnicalException;
import by.andruhovich.client.exception.socket.CreateSocketTechnicalException;
import by.andruhovich.client.exception.socket.ReceiveDataTechnicalException;
import by.andruhovich.client.socket.TCPSocket;
import by.andruhovich.client.type.CommandType;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

public class TCPCommandAction {
    private static final int CONNECT_ATTEMPT_COUNT = 10;
    private static final int RECONNECT_INTERVAL = 10;

    public static int packetNumber = 0;

    public static TCPSocket connect(InetAddress ipAddress, int port) throws AttemptCreateSocketTechnicalException {
        int connectAttemptCount = 0;
        TCPSocket tcpSocket;
        while (connectAttemptCount < CONNECT_ATTEMPT_COUNT) {
            try {
                tcpSocket = new TCPSocket(ipAddress, port);
                packetNumber = 0;
                if (handshake(tcpSocket)) {
                    return tcpSocket;
                }
                tcpSocket.closeSocket();
                connectAttemptCount++;
            } catch (CreateSocketTechnicalException e) {
                connectAttemptCount++;
                System.out.println("Attempt socket creating number " + connectAttemptCount + " failure!");
            }
        }
        throw new AttemptCreateSocketTechnicalException("Attempt socket creating count is exceeded!");
    }

    public static TCPSocket reconnect(TCPSocket tcpSocket) throws AttemptCreateSocketTechnicalException {
        System.out.println("Try to reconnect...");
        try {
            InetAddress ipAddress = tcpSocket.getInetAddress();
            int port = tcpSocket.getPort();
            tcpSocket.closeSocket();
            TimeUnit.SECONDS.sleep(RECONNECT_INTERVAL);
            tcpSocket = connect(ipAddress, port);
            return tcpSocket;
        } catch (InterruptedException e) {
            throw new AttemptCreateSocketTechnicalException(e);
        }
    }

    private static boolean handshake(TCPSocket tcpSocket) {
        int handshakeCount = 0;
        String dataForServer;
        String dataFromServer;

        while (handshakeCount == 0) {
            packetNumber++;
            dataForServer = packetNumber + " " + CommandType.HANDSHAKE.name();
            tcpSocket.sendData(dataForServer);
            try {
                dataFromServer = tcpSocket.receiveStringData();
                packetNumber = CommandParser.getPacketNumber(dataFromServer);
                CommandType handshakeCommand = CommandParser.getCommandType(dataFromServer);
                System.out.println(handshakeCommand + ":)");
            } catch (ReceiveDataTechnicalException e) {
                System.out.println(e.getMessage());
                return false;
            }
            CommandType commandType = CommandParser.getCommandType(dataFromServer);
            if (commandType.equals(CommandType.HANDSHAKE)) {
                handshakeCount++;
                packetNumber++;
                dataForServer = packetNumber + " " + CommandType.HANDSHAKE.name();
                tcpSocket.sendData(dataForServer);
            }
        }
        return true;
    }
}
