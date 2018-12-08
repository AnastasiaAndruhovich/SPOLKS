package by.andruhovich.multicastchat.service;

import by.andruhovich.multicastchat.console.ConsoleReader;
import by.andruhovich.multicastchat.console.ConsoleWriter;
import by.andruhovich.multicastchat.exception.CreateSocketTechnicalException;
import by.andruhovich.multicastchat.exception.SendDataTechnicalException;
import by.andruhovich.multicastchat.exception.SocketTechnicalException;
import by.andruhovich.multicastchat.service.constant.SubscriptionConstants;
import by.andruhovich.multicastchat.type.CommandType;

import java.util.concurrent.TimeUnit;

public class MulticastService {
    public static MulticastSender multicastSender;

    public void service() {
        MulticastListener multicastListener = null;
        boolean running = true;

        try {
            multicastListener = new MulticastListener();
            multicastSender = new MulticastSender();
            multicastListener.start();
            while (running) {
                ConsoleWriter.printLine("0 - exit");
                ConsoleWriter.printLine("1 - print address");
                ConsoleWriter.printLine("2 - subscribe to group");
                ConsoleWriter.printLine("3 - unsubscribe from group");
                ConsoleWriter.printLine("other - message");
                String line = ConsoleReader.getLine();
                CommandType commandType = defineCommandType(line);
                try {
                    switch (commandType) {
                        case PRINT:
                            multicastSender.sendMessage("request");
                            try {
                                TimeUnit.SECONDS.sleep(5);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            System.out.println(SubscriptionConstants.groupMembers);
                            break;
                        case SUBSCRIBE:
                            multicastListener.subscribeToGroup();
                            break;
                        case UNSUBSCRIBE:
                            multicastListener.unsubscribeFromGroup();
                            break;
                        case MESSAGE:
                            multicastSender.sendMessage(line);
                            break;
                        case EXIT:
                            ConsoleReader.close();
                            multicastSender.close();
                            multicastListener.interrupt();
                            running = false;
                            break;
                        default:
                            ConsoleWriter.printLine("Incorrect input!");
                    }
                } catch (SendDataTechnicalException | SocketTechnicalException e) {
                    ConsoleWriter.printLine(e.getMessage());
                }
            }
        } catch (CreateSocketTechnicalException /*| SocketTechnicalException*/ e) {
            ConsoleWriter.printLine(e.getMessage());
            ConsoleReader.close();
            if (multicastListener != null) {
                multicastListener.interrupt();
            }
        }
    }

    private CommandType defineCommandType(String line) {
        switch (line) {
            case "0":
                return CommandType.EXIT;
            case "1":
                return CommandType.PRINT;
            case "2":
                return CommandType.SUBSCRIBE;
            case "3":
                return CommandType.UNSUBSCRIBE;
            default:
                return CommandType.MESSAGE;
        }
    }
}
