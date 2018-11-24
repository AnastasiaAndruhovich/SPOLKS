package by.andruhovich.client.start;

import by.andruhovich.client.console.ConsolePrinter;
import by.andruhovich.client.service.ManagerSocketService;
import by.andruhovich.client.service.TCPSocketService;

import java.net.InetAddress;

public class Client {
    public static void main(String[] args) {
        InetAddress serverIP = ConsolePrinter.enterIP();
        ManagerSocketService managerSocketService = new ManagerSocketService();
        int serverPort = managerSocketService.serviceSocket(serverIP);
        if (serverPort != 0) {
            TCPSocketService TCPSocketService = new TCPSocketService();
            TCPSocketService.serviceSocket(serverPort, serverIP);
        }
    }
}
