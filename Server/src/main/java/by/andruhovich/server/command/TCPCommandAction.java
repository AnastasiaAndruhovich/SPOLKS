package by.andruhovich.server.command;

import by.andruhovich.server.exception.socket.SendDataTechnicalException;
import by.andruhovich.server.exception.socket.SocketTimeoutTechnicalException;
import by.andruhovich.server.socket.TCPSocket;

import java.net.Socket;
import java.net.SocketException;

public class TCPCommandAction {
    private static final int TIMEOUT = 300000;
    private static final int ATTEMPT_COUNT = 10;

    public static void sendData(TCPSocket tcpSocket, Socket clientSocket, String dataForClient) throws SocketTimeoutTechnicalException {
        boolean isClientReadPacket = false;
        int attemptNumber = 0;

        while (!isClientReadPacket && attemptNumber < ATTEMPT_COUNT) {
            try {
                clientSocket.setSoTimeout(TIMEOUT);
                tcpSocket.sendData(dataForClient);
                isClientReadPacket = true;

            } catch (SocketException e) {
                attemptNumber++;
                System.out.println(attemptNumber + ". Command timeout!");
            }
        }
        if (attemptNumber == ATTEMPT_COUNT) {
            throw new SocketTimeoutTechnicalException("Attempt count is exceeded");
        }
    }

    public static void sendData(TCPSocket tcpSocket, Socket clientSocket, byte[] dataForClient, int dataSize) throws SocketTimeoutTechnicalException {
        boolean isClientReadPacket = false;
        int attemptNumber = 0;

        while (!isClientReadPacket && attemptNumber < ATTEMPT_COUNT) {
            try {
                clientSocket.setSoTimeout(TIMEOUT);
                tcpSocket.sendData(dataForClient, dataSize);
                isClientReadPacket = true;

            } catch (SocketException | SendDataTechnicalException e) {
                attemptNumber++;
                System.out.println(attemptNumber + ". Command timeout!");
            }
        }
        if (attemptNumber == ATTEMPT_COUNT) {
            throw new SocketTimeoutTechnicalException("Attempt count is exceeded");
        }
    }
}
