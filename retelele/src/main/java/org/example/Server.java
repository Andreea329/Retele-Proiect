package org.example;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class Server {
    private static final String DB_URL = "jdbc:sqlite:identifier.sqlite";

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(12345);
            System.out.println("Server started. Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                // Handle client in a separate thread
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private Connection connection;
        private PrintWriter writer;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                // Establish database connection
                this.connection = DriverManager.getConnection(DB_URL);
                // Create PrintWriter for current client
                this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // Read client requests and respond accordingly
                String request;
                while ((request = reader.readLine()) != null) {
                    // Process client request
                    String[] parts = request.split(" ");
                    String command = parts[0];

                    switch (command) {
                        case "GET":
                            handleGetRequest(parts, writer);
                            break;
                        case "UPDATE":
                            handleUpdateRequest(parts, writer);
                            break;
                        case "GET_ALL":
                            handleGetAllRequest(writer);
                            break;
                        case "DELETE":
                            handleDeleteRequest(parts, writer);
                            break;
                        case "SUBSCRIBE":
                            handleSubscribeRequest(parts, writer);
                            break;
                        case "UNSUBSCRIBE":
                            handleUnsubscribeRequest(parts, writer);
                            break;
                        default:
                            writer.println("Invalid command");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                    clientSocket.close();
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleUpdateRequest(String[] parts, PrintWriter writer) {
            try {
                String productId = parts[1];
                double newPrice = Double.parseDouble(parts[2]);
                String updateQuery = "UPDATE products SET price = ? WHERE product_id  = ?";
                PreparedStatement statement = connection.prepareStatement(updateQuery);
                statement.setDouble(1, newPrice);
                statement.setString(2, productId);
                int rowsUpdated = statement.executeUpdate();

                if (rowsUpdated > 0) {
                    writer.println("Product updated successfully");
                    ProductManager.notifyUpdate(productId, newPrice);

                } else {
                    writer.println("Product not found");
                }
            } catch (SQLException | NumberFormatException e) {
                e.printStackTrace();
            }
        }

        private void handleSubscribeRequest(String[] parts, PrintWriter writer) {
            String productId = parts[1];
            ProductManager.subscribe(productId, writer);
            writer.println("Subscribed to product updates for ID: " + productId);
        }

        private void handleUnsubscribeRequest(String[] parts, PrintWriter writer) {
            String productId = parts[1];
            ProductManager.unsubscribe(productId, writer);
            writer.println("Unsubscribed from product updates for ID: " + productId);
        }

        private void handleGetRequest(String[] parts, PrintWriter writer) {
            try {
                String productId = parts[1];
                String query = "SELECT * FROM products WHERE product_id = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, productId);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    String name = resultSet.getString("name");
                    String description = resultSet.getString("description");
                    double price = resultSet.getDouble("price");
                    writer.println(name + " | " + description + " | " + price + "\n Chose another option: ");
                } else {
                    writer.println("Product not found");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void handleGetAllRequest(PrintWriter writer) {
            try {
                String query = "SELECT * FROM products";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);

                StringBuilder response = new StringBuilder();
                while (resultSet.next()) {
                    String productId = resultSet.getString("product_id");
                    String name = resultSet.getString("name");
                    String description = resultSet.getString("description");
                    double price = resultSet.getDouble("price");
                    response.append(productId).append(" | ").append(name).append(" | ").append(description).append(" | ").append(price).append("\n");
                }

                if (response.length() > 0) {
                    System.out.println("Server response: " + response.toString() );
                    writer.println(response.toString()+ "\n Chose another option: ");
                } else {
                    writer.println("No products found");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        private void handleDeleteRequest(String[] parts, PrintWriter writer) {
            try {
                String productId = parts[1];
                String deleteQuery = "DELETE FROM products WHERE product_id = ?";
                PreparedStatement statement = connection.prepareStatement(deleteQuery);
                statement.setString(1, productId);
                int rowsDeleted = statement.executeUpdate();

                if (rowsDeleted > 0) {
                    writer.println("Product deleted successfully"+ "\n Chose another option: ");
                } else {
                    writer.println("Product not found");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}