package org.example;

import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProductManager {
    private static final Map<String, Set<PrintWriter>> subscriptions = new ConcurrentHashMap<>();

    public static synchronized void subscribe(String productId, PrintWriter writer) {
        subscriptions.putIfAbsent(productId, new HashSet<>());
        subscriptions.get(productId).add(writer);
    }

    public static synchronized void unsubscribe(String productId, PrintWriter writer) {
        Set<PrintWriter> subscribers = subscriptions.get(productId);
        if (subscribers != null) {
            subscribers.remove(writer);
            if (subscribers.isEmpty()) {
                subscriptions.remove(productId);
            }
        }
    }

    public static synchronized void notifyUpdate(String productId, double newPrice) {
        Set<PrintWriter> subscribers = subscriptions.get(productId);
        if (subscribers != null) {
            for (PrintWriter subscriber : subscribers) {
                subscriber.println("Product with ID " + productId + " was updated. New price: " + newPrice + "\n Chose another option: ");
            }
        }
    }
}
