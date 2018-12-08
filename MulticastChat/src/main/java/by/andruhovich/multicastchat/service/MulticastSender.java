package by.andruhovich.multicastchat.service;

import by.andruhovich.multicastchat.exception.CreateSocketTechnicalException;
import by.andruhovich.multicastchat.exception.SendDataTechnicalException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MulticastSender {
    private DatagramSocket socket;
    private InetAddress group;

    private static final int PORT_NUMBER = 6000;
    private static final String GROUP_ADDRESS = "230.0.0.0";

    public MulticastSender() throws CreateSocketTechnicalException {
        try {
            socket = new DatagramSocket();
            group = InetAddress.getByName(GROUP_ADDRESS);
        } catch (IOException e) {
            throw new CreateSocketTechnicalException("Create socket error");
        }
    }

    public void sendMessage(String message) throws SendDataTechnicalException {
        byte[] data = message.getBytes();
        sendData(data);
    }

    public void close() {
        socket.close();
    }

    private void sendData(byte[] data) throws SendDataTechnicalException {
        DatagramPacket packet = new DatagramPacket(data, data.length, group, PORT_NUMBER);
        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new SendDataTechnicalException("Send data error");
        }
    }
}
