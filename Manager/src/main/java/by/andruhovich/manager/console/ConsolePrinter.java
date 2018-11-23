package by.andruhovich.manager.console;

import static by.andruhovich.manager.console.ConsoleReader.getLine;

public class ConsolePrinter {

    public static int enterPort() {
        System.out.println("Enter the Port or RANDOM:");
        String portLine = getLine().toUpperCase();
        int port;
        switch (portLine) {
            case "RANDOM": {
                port = generatePort();
                break;
            }
            default: {
                port = Integer.parseInt(portLine);
            }
        }
        return port;
    }

    private static int generatePort() {
        return (int) (Math.random() * 63511 + 1024);
    }
}
