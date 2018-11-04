package by.andruhovich.server.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleReader {
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static String getLine()
    {
        while (true) {
            try {
                return reader.readLine();
            } catch (IOException e) {
                System.out.println("Incorrect input!");
            }
        }
    }

    public static void close() {
        try {
            reader.close();
        } catch (IOException e) {
            System.out.println("Console reader closing failure!");
        }
    }
}
