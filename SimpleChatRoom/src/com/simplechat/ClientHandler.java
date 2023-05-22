package com.simplechat;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter output;
    private BufferedReader input;
    private List<ClientHandler> clients;
    private String nickname;

    public Socket getClientSocket() {
        return clientSocket;
    }

    public ClientHandler(Socket socket, List<ClientHandler> clients) {
        this.clientSocket = socket;
        this.clients = clients;
    }

    private void broadcastMessage(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = sdf.format(new Date());
        String messageWithTimestamp = "[" + timestamp + "] " + nickname + ": " + message;

        for (ClientHandler client : clients) {
            // Check if the message is the stop message from the server
            if (message.equals("/stop")) {
                client.disconnect();
            }
            else
                client.output.println(messageWithTimestamp);
        }
    }

    public void run() {
        try {
            output = new PrintWriter(clientSocket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String message;
            while ((message = input.readLine()) != null) {
                if (message.equalsIgnoreCase("/disconnect")) {
                    disconnect();
                    break;
                } else if (message.startsWith("/nickname ")) {
                    String newNickname = message.substring(10);
                    if (newNickname.isEmpty()) {
                        output.println("Nickname cannot be empty.");
                    } else if (checkNicknameAvailability(newNickname)) {
                        setNickname(newNickname);
                        output.println("/nickname " + newNickname);
                    } else {
                        output.println("Nickname already taken. Please choose a different nickname.");
                    }
                } else {
                    System.out.println("Received message from client " + nickname + ": " + message);
                    broadcastMessage(message);
                }
            }

            // Client disconnected
            System.out.println("Client disconnected: " + clientSocket);
            clients.remove(this);
            clientSocket.close();
        } catch (IOException e) {
            // Client disconnected unexpectedly
            System.out.println("Client disconnected unexpectedly: " + clientSocket);
            clients.remove(this);
        }
    }

    private void setNickname(String newNickname) {
        nickname = newNickname;
        System.out.println("Client " + clientSocket + " set nickname to: " + nickname);
    }

    private void disconnect() {
        System.out.println("Client " + clientSocket + " requested disconnection.");
        output.println("You are now disconnected from the server.");
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            clients.remove(this);
        }
    }


    private boolean checkNicknameAvailability(String newNickname) {
        for (ClientHandler client : clients) {
            if (client != this && client.nickname != null && client.nickname.equals(newNickname)) {
                return false;
            }
        }
        return true;
    }
}
