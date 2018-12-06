package by.andruhovich.broadcastchat.console;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConsoleWriter {
    private static Lock lock = new ReentrantLock();
    private static Condition isFree = lock.newCondition();

    public static void printLine(String line) {
        try {
            lock.lock();
            System.out.println(line);
            isFree.signal();
        } finally {
            lock.unlock();
        }

    }
}
