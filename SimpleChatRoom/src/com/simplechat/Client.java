package com.simplechat;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;

public class Client {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String nickname;

    private Thread receiveThread;

    private boolean isConnected;

    public Client(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            isConnected = true;
            System.out.println("Connected to server: " + socket);

            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

        } catch (ConnectException e) {
            System.out.println("Connection to the server cannot be made. Make sure the server is started!");
            // You can choose to exit the program or handle the error in a different way
            System.exit(1);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void start() {
        try {
            // Start a new thread to listen for messages from the server
            receiveThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = input.readLine()) != null) {
                        if (message.startsWith("/nickname")) {
                            setNicknameFromServer(message.substring(10));
                        } else {
                            System.out.println(message);
                        }
                    }
                } catch (IOException e) {
                    // Handle the SocketException here when the server is stopped
                    if (e instanceof SocketException) {
                        System.out.println("Server has stopped. Disconnected from the server.");
                    } else {
                        e.printStackTrace();
                    }
                    this.isConnected = false;
                    System.exit(0);
                }
            });

            receiveThread.start();

            // Read user input and send messages to the server
            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
            String message;
            while ((message = consoleInput.readLine()) != null && this.isConnected == true) {
                if (message.equalsIgnoreCase("/disconnect")) {
                    disconnect();
                    break;
                } else if (message.startsWith("/nickname ")) {
                    String newNickname = message.substring(10);
                    if (newNickname.isEmpty()) {
                        System.out.println("Nickname cannot be empty.");
                    } else {
                        setNickname(newNickname);
                    }
                } else {
                    if (nickname == null) {
                        System.out.println("Please set a nickname first.");
                    } else {
                        sendMessage(message);
                    }
                }
            }

            // Close the client socket when the input loop ends
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String message) {
        output.println(message);
    }

    private void setNickname(String newNickname) {
        output.println("/nickname " + newNickname);
    }

    private void setNicknameFromServer(String newNickname) {
        nickname = newNickname;
        System.out.println("Nickname set to: " + nickname);
    }

    private void disconnect() {
        System.out.println("Disconnecting from the server...");
        sendMessage("/disconnect");
        this.isConnected = false;

        // Wait for the receive thread to finish reading messages
        try {
            receiveThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client("localhost", 12345);
        client.start();
    }
}
