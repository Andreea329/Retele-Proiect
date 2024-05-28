package org.example;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {

    public static void main(String[] args) {
        Socket socket = null;
        BufferedReader reader = null;
        PrintWriter writer = null;
        BufferedReader userInputReader = null;
        AtomicBoolean receivedNotification = new AtomicBoolean(false);

        try {
            socket = new Socket("localhost", 12345);
            System.out.println("Connected to server.");

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            userInputReader = new BufferedReader(new InputStreamReader(System.in));

            // Subscribing to product updates
            System.out.print("Enter product ID to subscribe: ");
            String productId = userInputReader.readLine();
            writer.println("SUBSCRIBE " + productId);
            System.out.println(reader.readLine()); // Server response

            // Listening for server responses
            // Listening for server responses
            // Listening for server responses
            BufferedReader finalReader = reader;
            PrintWriter finalWriter = writer;
            new Thread(() -> {
                try {
                    String serverResponse;
                    while ((serverResponse = finalReader.readLine()) != null) {
                        if (serverResponse.startsWith("Server notification:")) {
                            System.out.println(serverResponse);
                            receivedNotification.set(true);
                        } else {
                            if (!receivedNotification.get()) {
                                System.out.print(serverResponse);
                                System.out.print("\n");
                                // Flush the output stream to ensure data is sent immediately
                                finalWriter.flush();
                            }
                            // Reset the receivedNotification flag
                            receivedNotification.set(false);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    // After processing all server responses, prompt for new option
                    System.out.print("Select a new option: ");
                }
            }).start();

            // Displaying menu options
            System.out.println("Welcome to Our Shop! Select an action: ");
            System.out.println("1. Get product by ID");
            System.out.println("2. Get all products");
            System.out.println("3. Update product price by ID");
            System.out.println("4. Delete product by ID");
            System.out.println("Select an option: ");

            // Sending other requests (GET, UPDATE, DELETE, etc.)
            while (true) {

                int option = Integer.parseInt(userInputReader.readLine());

                switch (option) {
                    case 1:
                        System.out.print("Enter product ID to retrieve: ");
                        String getProductById = userInputReader.readLine();
                        writer.println("GET " + getProductById);
                        break;
                    case 2:
                        writer.println("GET_ALL");
                        break;
                    case 3:
                        System.out.print("Enter product ID to update: ");
                        String updateProductId = userInputReader.readLine();
                        System.out.print("Enter new price: ");
                        double newPrice = Double.parseDouble(userInputReader.readLine());
                        writer.println("UPDATE " + updateProductId + " " + newPrice);
                        break;
                    case 4:
                        System.out.print("Enter product ID to delete: ");
                        String deleteProductId = userInputReader.readLine();
                        writer.println("DELETE " + deleteProductId);
                        break;
                    default:
                        System.out.println("Invalid option");
                        break;
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
