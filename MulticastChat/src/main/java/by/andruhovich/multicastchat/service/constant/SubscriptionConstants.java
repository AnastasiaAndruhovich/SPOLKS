package by.andruhovich.multicastchat.service.constant;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SubscriptionConstants {
    public static AtomicBoolean isSubscribed = new AtomicBoolean(true);

    private static Lock lock = new ReentrantLock();
    private static Condition isFree = lock.newCondition();

    public static void subscribe() {
        try {
            lock.lock();
            isSubscribed = new AtomicBoolean(true);
            isFree.signal();
        } finally {
            lock.unlock();
        }
    }

    public static void unsubscribe() {
        try {
            lock.lock();
            isSubscribed = new AtomicBoolean(false);
            isFree.signal();
        } finally {
            lock.unlock();
        }
    }
}
