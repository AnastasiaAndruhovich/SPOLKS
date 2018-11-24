package by.andruhovich.server.start;

import by.andruhovich.server.service.TCPSocketService;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.TimeUnit;

public class Server {
    private static FileLock fileLock;

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        takeFile(port);
        TCPSocketService socketService = new TCPSocketService();
        socketService.serviceSocket(port);
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        returnFile();
    }

    private static void takeFile(int port) {
        String filename = String.valueOf(port) + ".txt";
        File file = new File(filename);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
            FileChannel fileChannel = accessFile.getChannel();
            fileLock = fileChannel.tryLock();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void returnFile() {
        try {
            fileLock.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
