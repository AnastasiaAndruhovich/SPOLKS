package by.andruhovich.client.console;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static by.andruhovich.client.console.ConsoleReader.getLine;

public class ConsolePrinter {

    public static InetAddress enterIP() {
        System.out.println("Enter IP: ");
        while (true) {
            try {
                return InetAddress.getByName(getLine());
            } catch (UnknownHostException e) {
                System.out.println("Input error! Try again.");
            }
        }
    }

    public static int enterPort() {
        System.out.println("Enter port: ");
        while (true) {
            try {
                return Integer.parseInt(getLine());
            } catch (NumberFormatException e) {
                System.out.println("Input error! Try again.");
            }
        }
    }

    public static int chooseMode() {
        System.out.println("Choose mode:");
        System.out.println("1 - TCP");
        System.out.println("2 - UDP");
        System.out.println("0 - Exit");
        String answer = getLine();
        return Integer.parseInt(answer);
    }

}
