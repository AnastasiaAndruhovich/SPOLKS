package by.andruhovich.server.socket;

import by.andruhovich.server.exception.socket.CreateSocketTechnicalException;
import by.andruhovich.server.exception.socket.ReceiveDataTechnicalException;
import by.andruhovich.server.exception.socket.SendDataTechnicalException;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class UDPSocket {
    private DatagramSocket serverSocket;
    private DatagramPacket inPacket;
    private DatagramPacket outPacket;
    private InetAddress clientAddress;
    private int clientPort;

    private byte[] buffer;
    private static final int BUFFER_SIZE = 1500;
    private static final int TIMEOUT = 30000;

    public UDPSocket(int port) throws CreateSocketTechnicalException {
        try {
            serverSocket = new DatagramSocket(port);
            serverSocket.setSoTimeout(TIMEOUT);
            serverSocket.setReceiveBufferSize(BUFFER_SIZE);
            serverSocket.setReceiveBufferSize(BUFFER_SIZE);
            buffer = new byte[BUFFER_SIZE];
            inPacket = new DatagramPacket(buffer, BUFFER_SIZE);

            System.out.println("Address: " + InetAddress.getLocalHost());
            System.out.println("Port: " + port);
        } catch (SocketException | UnknownHostException e) {
            throw new CreateSocketTechnicalException("Creating server socket failure! Port: " + port);
        }
    }

    public boolean isOldClient() {
        return inPacket.getAddress().equals(clientAddress);
    }

    public boolean isOldPort() {
        return inPacket.getPort() == clientPort;
    }

    public void setNewClient() {
        clientAddress = inPacket.getAddress();
        clientPort = inPacket.getPort();
        outPacket = new DatagramPacket(buffer, BUFFER_SIZE, clientAddress, clientPort);
        System.out.println("Client connected. Address: " + clientAddress.getHostAddress() + ". Port: " + clientPort);
    }

    public void setNewPort() {
        clientPort = inPacket.getPort();
        outPacket = new DatagramPacket(buffer, BUFFER_SIZE, clientAddress, clientPort);
    }

    public String receiveData() throws ReceiveDataTechnicalException {
        try {
            Arrays.fill(buffer, (byte) 0);
            serverSocket.receive(inPacket);
            return new String(inPacket.getData()).replaceAll("\0", "");
        } catch (IOException e) {
            throw new ReceiveDataTechnicalException("Receiving data failure!");
        }
    }

    public void sendData(byte[] data, int size) throws SendDataTechnicalException {
        outPacket.setData(data);
        outPacket.setLength(size);
        try {
            serverSocket.send(outPacket);
        } catch (IOException e) {
            throw new SendDataTechnicalException("Sending file failure");
        }
    }

    public void closeSocket() {
        serverSocket.close();
    }

}
