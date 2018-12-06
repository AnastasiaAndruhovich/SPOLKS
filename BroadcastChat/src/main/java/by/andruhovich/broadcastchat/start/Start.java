package by.andruhovich.broadcastchat.start;

import by.andruhovich.broadcastchat.service.BroadcastService;

public class Start {
    public static void main(String[] args) {
        BroadcastService broadcastService = new BroadcastService();
        broadcastService.service();
        System.exit(0);
    }
}
