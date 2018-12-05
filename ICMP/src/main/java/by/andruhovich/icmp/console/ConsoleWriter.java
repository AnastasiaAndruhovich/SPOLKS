package by.andruhovich.icmp.console;

import static by.andruhovich.icmp.console.ConsoleReader.getLine;

public class ConsoleWriter {
    public static int chooseMode() {
        System.out.println("Choose mode:");
        System.out.println("1 - ping");
        System.out.println("2 - smurf attack");
        System.out.println("0 - Exit");
        String answer = getLine();
        return Integer.parseInt(answer);
    }
}
