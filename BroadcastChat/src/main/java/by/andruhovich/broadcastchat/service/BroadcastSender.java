package by.andruhovich.broadcastchat.service;

import by.andruhovich.broadcastchat.console.ConsoleWriter;
import by.andruhovich.broadcastchat.exception.CreateSocketTechnicalException;
import by.andruhovich.broadcastchat.exception.SendDataTechnicalException;
import by.andruhovich.broadcastchat.exception.SocketTechnicalException;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

public class BroadcastSender {
    private DatagramSocket socket;
    private static final int PORT_NUMBER = 5000;

    public BroadcastSender() throws CreateSocketTechnicalException {
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
        } catch (SocketException e) {
            throw new CreateSocketTechnicalException("Create socket error");
        }
    }

    public void sendMessage(String message) throws SocketTechnicalException, SendDataTechnicalException {
        byte[] data = message.getBytes();
        List<InetAddress> addresses = getAllBroadcastAddresses();
        for (InetAddress address : addresses) {
            sendData(data, address);
        }
        /*try {
            sendData(data, InetAddress.getByName("255.255.255.255"));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }*/
    }

    public void printAllBroadcastAddresses() throws SocketTechnicalException {
        List<InetAddress> addresses = getAllBroadcastAddresses();
        for (InetAddress address : addresses) {
            ConsoleWriter.printLine(address.getHostAddress());
        }
    }

    public void close() {
        socket.close();
    }

    private List<InetAddress> getAllBroadcastAddresses() throws SocketTechnicalException {
        List<InetAddress> broadcastList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                broadcastList.addAll(networkInterface.getInterfaceAddresses()
                        .stream()
                        .filter(address -> address.getBroadcast() != null)
                        .map(InterfaceAddress::getBroadcast)
                        .collect(Collectors.toList()));
            }
            return broadcastList;
        } catch (SocketException e) {
            throw new SocketTechnicalException("Error socket execution");
        }
    }

    private void sendData(byte[] data, InetAddress address) throws SendDataTechnicalException {
        DatagramPacket packet = new DatagramPacket(data, data.length, address, PORT_NUMBER);
        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new SendDataTechnicalException("Send data error");
        }
    }
}
