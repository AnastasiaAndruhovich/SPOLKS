package by.andruhovich.client.start;

import by.andruhovich.client.console.ConsolePrinter;
import by.andruhovich.client.service.TCPSocketService;
import by.andruhovich.client.service.UDPSocketService;

public class Client {
    public static void main(String[] args) {
        int mode = ConsolePrinter.chooseMode();
        switch (mode) {
            case 1:
                TCPSocketService TCPSocketService = new TCPSocketService();
                TCPSocketService.serviceSocket();
                break;
            case 2:
                UDPSocketService udpSocketService = new UDPSocketService();
                udpSocketService.serviceSocket();
                break;
        }
    }
}
