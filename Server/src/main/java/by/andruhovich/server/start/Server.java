package by.andruhovich.server.start;

import by.andruhovich.server.console.ConsolePrinter;
import by.andruhovich.server.service.MultipleTCPSocketService;
import by.andruhovich.server.service.TCPSocketService;
import by.andruhovich.server.service.UDPSocketService;

public class Server {
    public static void main(String[] args) {
        /*try {
            ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", "Client.jar", "hello world");
            final Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        int mode = ConsolePrinter.chooseMode();
        switch (mode) {
            case 1:
                TCPSocketService socketService = new TCPSocketService();
                socketService.serviceSocket();
                break;
            case 2:
                UDPSocketService udpSocketService = new UDPSocketService();
                udpSocketService.serviceSocket();
                break;
            case 3:
                MultipleTCPSocketService multipleTCPSocketService = new MultipleTCPSocketService();
                multipleTCPSocketService.serviceSocket();
        }
    }
}
