package by.andruhovich.multicastchat.service;

import by.andruhovich.multicastchat.exception.CreateSocketTechnicalException;
import by.andruhovich.multicastchat.exception.SendDataTechnicalException;
import by.andruhovich.multicastchat.exception.SocketTechnicalException;
import by.andruhovich.multicastchat.service.constant.SubscriptionConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastSender {
    private MulticastSocket socket;
    private InetAddress group;

    private static final int PORT_NUMBER = 6000;
    private static final String GROUP_ADDRESS = "230.0.0.0";

    public MulticastSender() throws CreateSocketTechnicalException, SocketTechnicalException {
        try {
            socket = new MulticastSocket();
            group = InetAddress.getByName(GROUP_ADDRESS);
            subscribeToGroup();
        } catch(IOException e) {
            throw new CreateSocketTechnicalException("Create socket error");
        }
    }

    public void sendMessage(String message) throws SendDataTechnicalException {
        byte[] data = message.getBytes();
        if (SubscriptionConstants.isSubscribed.get()) {
            sendData(data);
        } else {
            throw  new SendDataTechnicalException("You are not subscribe to group");
        }
    }

    public void subscribeToGroup() throws SocketTechnicalException {
        try {
            socket.joinGroup(group);
            SubscriptionConstants.subscribe();
            System.out.println("Socket subscribed to group " + group.getHostAddress());
        } catch (IOException e) {
            throw new SocketTechnicalException("Socket subscribe to group " + group.getHostAddress() + " error");
        }
    }

    public void unsubscribeFromGroup() throws SocketTechnicalException {
        try {
            socket.leaveGroup(group);
            SubscriptionConstants.unsubscribe();
            System.out.println("Socket unsubscribed from group " + group.getHostAddress());
        } catch (IOException e) {
            throw new SocketTechnicalException("Socket unsubscribe from group " + group.getHostAddress() + " error");
        }
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
