package by.andruhovich.multicastchat.start;

import by.andruhovich.multicastchat.service.MulticastService;

public class Start {
    public static void main(String[] args) {
        MulticastService multicastService = new MulticastService();
        multicastService.service();
        System.exit(0);
    }
}
