package by.andruhovich.client.socket;

import by.andruhovich.client.exception.socket.CreateSocketTechnicalException;
import by.andruhovich.client.exception.socket.ReceiveDataTechnicalException;
import by.andruhovich.client.exception.socket.SendDataTechnicalException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

public class UDPSocket {
    private DatagramSocket clientSocket;
    private DatagramPacket inPacket;
    private DatagramPacket outPacket;
    private InetAddress serverAddress;
    private int serverPort;

    private byte[] buffer;
    private static final int BUFFER_SIZE = 1500;
    private static final int TIMEOUT = 300000;

    public UDPSocket(InetAddress address, int port) throws CreateSocketTechnicalException {
        try {
            clientSocket = new DatagramSocket();
            clientSocket.setSoTimeout(TIMEOUT);
            clientSocket.setReceiveBufferSize(BUFFER_SIZE);
            clientSocket.setReceiveBufferSize(BUFFER_SIZE);
            buffer = new byte[BUFFER_SIZE];
            inPacket = new DatagramPacket(buffer, BUFFER_SIZE);
            outPacket = new DatagramPacket(buffer, BUFFER_SIZE, address, port);

            serverAddress = address;
            serverPort = port;

        } catch (SocketException e) {
            throw new CreateSocketTechnicalException("Creating client socket failure!");
        }
    }

    public void setServer(InetAddress address, int port) {
        serverPort = port;
        serverAddress = address;
    }

    public String receiveStringData() throws ReceiveDataTechnicalException {
        try {
            Arrays.fill(buffer, (byte) 0);
            clientSocket.receive(inPacket);
            return new String(inPacket.getData()).replaceAll("\0", "");
        } catch (IOException e) {
            throw new ReceiveDataTechnicalException("Receiving data failure!");
        }
    }

    public byte[] receiveByteData() throws ReceiveDataTechnicalException {
        try {
            Arrays.fill(buffer, (byte) 0);
            clientSocket.receive(inPacket);
            return inPacket.getData();
        } catch (IOException e) {
            throw new ReceiveDataTechnicalException("Receiving data failure!");
        }
    }

    public void sendData(byte[] data, int size) throws SendDataTechnicalException {
        outPacket.setData(data);
        outPacket.setLength(size);
        try {
            clientSocket.send(outPacket);
        } catch (IOException e) {
            throw new SendDataTechnicalException("Sending file failure");
        }
    }

    public void closeSocket() {
        clientSocket.close();
    }
}
