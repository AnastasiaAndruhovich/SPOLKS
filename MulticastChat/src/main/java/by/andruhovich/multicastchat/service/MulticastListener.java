package by.andruhovich.multicastchat.service;

import by.andruhovich.multicastchat.console.ConsoleWriter;
import by.andruhovich.multicastchat.exception.CreateSocketTechnicalException;
import by.andruhovich.multicastchat.exception.ReceiveDataTechnicalException;
import by.andruhovich.multicastchat.exception.SendDataTechnicalException;
import by.andruhovich.multicastchat.exception.SocketTechnicalException;
import by.andruhovich.multicastchat.service.constant.SubscriptionConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.Objects;

public class MulticastListener extends Thread {
    private MulticastSocket socket;
    private InetAddress group;
    private byte[] buffer;
    private DatagramPacket packet;

    private static final int BUFFER_SIZE = 1024;
    private static final int PORT_NUMBER = 6000;
    private static final String GROUP_ADDRESS = "230.0.0.0";

    public MulticastListener() throws CreateSocketTechnicalException {
        try {
            socket = new MulticastSocket(PORT_NUMBER);
            socket.setReuseAddress(true);
            group = InetAddress.getByName(GROUP_ADDRESS);
            socket.joinGroup(group);

            buffer = new byte[BUFFER_SIZE];
            packet = new DatagramPacket(buffer, BUFFER_SIZE);
        } catch (IOException e) {
            throw new CreateSocketTechnicalException("Create socket error");
        }
    }

    @Override
    public void run() {
        boolean running = true;
        while (running) {
            try {
                if (SubscriptionConstants.isSubscribed.get()) {
                    byte[] data = receivePacket();
                    String receivedData = convertData(data);
                    String packetAddress = packet.getAddress().getHostAddress();
                    String localAddress = Objects.requireNonNull(SubscriptionConstants.getLocalAddress()).getHostAddress();
                    if (!packetAddress.equals(localAddress)) {
                        ConsoleWriter.printLine("Client " + packet.getAddress() + ": " + receivedData);
                        if (receivedData.contains("request")) {
                            MulticastService.multicastSender.sendMessage("response");
                        } else if (receivedData.contains("response")) {
                            SubscriptionConstants.groupMembers.add(packet.getAddress());
                        }
                    }
                }
            } catch (ReceiveDataTechnicalException | SendDataTechnicalException e) {
                ConsoleWriter.printLine(e.getMessage());
                running = false;
            }
        }
        socket.close();
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

    private byte[] receivePacket() throws ReceiveDataTechnicalException {
        try {
            Arrays.fill(buffer, (byte) 0);
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
