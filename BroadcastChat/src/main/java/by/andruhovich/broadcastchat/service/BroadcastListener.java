package by.andruhovich.broadcastchat.service;

import by.andruhovich.broadcastchat.console.ConsoleWriter;
import by.andruhovich.broadcastchat.exception.CreateSocketTechnicalException;
import by.andruhovich.broadcastchat.exception.ReceiveDataTechnicalException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class BroadcastListener extends Thread{
    private DatagramSocket socket;
    private DatagramPacket packet;
    private byte[] buffer;

    private static final int BUFFER_SIZE = 1024;
    private static final int PORT_NUMBER = 5000;

    public BroadcastListener() throws CreateSocketTechnicalException {
        try {
            socket = new DatagramSocket(PORT_NUMBER);
            socket.setReuseAddress(true);
            buffer = new byte[BUFFER_SIZE];
            packet = new DatagramPacket(buffer, buffer.length);
        } catch (SocketException e) {
            throw new CreateSocketTechnicalException("Create socket error");
        }
    }

    @Override
    public void run() {
        boolean running = true;
        while (running) {
            try {
                byte[] data = receivePacket();
                String receivedData = convertData(data);
                ConsoleWriter.printLine("Client " + packet.getAddress() + ": " + receivedData);
            } catch (ReceiveDataTechnicalException e) {
                ConsoleWriter.printLine(e.getMessage());
                running = false;
            }
        }
        socket.close();
    }

    private byte[] receivePacket() throws ReceiveDataTechnicalException {
        try {
            socket.receive(packet);
            return packet.getData();
        } catch (IOException e) {
            throw new ReceiveDataTechnicalException("Receive data error");
        }
    }

    private String convertData(byte[] data) {
        return new String(data).replaceAll("\0", "");
    }
}
