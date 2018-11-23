package by.andruhovich.server.start;

import by.andruhovich.server.service.TCPSocketService;

import java.util.concurrent.TimeUnit;

public class Server {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        TCPSocketService socketService = new TCPSocketService();
        socketService.serviceSocket(port);
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
