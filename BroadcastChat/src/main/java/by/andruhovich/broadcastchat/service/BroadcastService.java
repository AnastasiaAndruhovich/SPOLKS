package by.andruhovich.broadcastchat.service;

import by.andruhovich.broadcastchat.console.ConsoleReader;
import by.andruhovich.broadcastchat.console.ConsoleWriter;
import by.andruhovich.broadcastchat.exception.CreateSocketTechnicalException;
import by.andruhovich.broadcastchat.exception.SendDataTechnicalException;
import by.andruhovich.broadcastchat.exception.SocketTechnicalException;
import by.andruhovich.broadcastchat.type.CommandType;

public class BroadcastService {
    public void service() {
        BroadcastListener broadcastListener = null;
        BroadcastSender broadcastSender;
        boolean running = true;

        try {
            broadcastListener = new BroadcastListener();
            broadcastSender = new BroadcastSender();
            broadcastListener.start();
            while (running) {
                ConsoleWriter.printLine("0 - exit\n1 - print address\nother - message");
                String line = ConsoleReader.getLine();
                CommandType commandType = defineCommandType(line);
                try {
                    switch (commandType) {
                        case PRINT:
                            broadcastSender.printAllBroadcastAddresses();
                            break;
                        case MESSAGE:
                            broadcastSender.sendMessage(line);
                            break;
                        case EXIT:
                            ConsoleReader.close();
                            broadcastSender.close();
                            broadcastListener.interrupt();
                            running = false;
                            break;
                        default:
                            ConsoleWriter.printLine("Incorrect input!");
                    }
                } catch (SocketTechnicalException | SendDataTechnicalException e) {
                    ConsoleWriter.printLine(e.getMessage());
                }
            }
        } catch (CreateSocketTechnicalException e) {
            ConsoleWriter.printLine(e.getMessage());
            ConsoleReader.close();
            if (broadcastListener != null) {
                broadcastListener.interrupt();
            }
        }
    }

    private CommandType defineCommandType(String line) {
        switch (line) {
            case "0":
                return CommandType.EXIT;
            case "1":
                return CommandType.PRINT;
            default:
                return CommandType.MESSAGE;
        }
    }
}
