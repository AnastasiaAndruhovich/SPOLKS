package by.andruhovich.client.start;

import by.andruhovich.client.service.ManagerSocketService;
import by.andruhovich.client.service.TCPSocketService;

public class Client {
    public static void main(String[] args) {
        ManagerSocketService managerSocketService = new ManagerSocketService();
        int serverPort = managerSocketService.serviceSocket();
        if (serverPort != 0) {
            TCPSocketService TCPSocketService = new TCPSocketService();
            TCPSocketService.serviceSocket(serverPort);
        }
    }
}
