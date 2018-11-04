package by.andruhovich.server.console;

import static by.andruhovich.server.console.ConsoleReader.getLine;

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

    public static boolean isServerWantToContinue() {
        System.out.println("Wait new Client? Y/N");
        String answer = getLine();
        return  answer.equalsIgnoreCase("Y");
    }

    public static int chooseMode() {
        System.out.println("Choose mode:");
        System.out.println("1 - TCP");
        System.out.println("2 - UDP");
        System.out.println("0 - Exit");
        String answer = getLine();
        return Integer.parseInt(answer);
    }

    private static int generatePort() {
        return (int) (Math.random() * 63511 + 1024);
    }
}
