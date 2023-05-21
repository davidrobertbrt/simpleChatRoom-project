package com.simplechat;

import java.io.*;
import java.net.*;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter output;
    private BufferedReader input;

    private List<ClientHandler> clients;

    public Socket getClientSocket() {
        return clientSocket;
    }

    public ClientHandler(Socket socket, List<ClientHandler> clients) {
        this.clientSocket = socket;
        this.clients = clients;
    }

    private void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            if (client != this) {
                client.output.println(message);
            }
        }
    }

    public void run() {
        try {
            output = new PrintWriter(clientSocket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String message;
            while ((message = input.readLine()) != null) {
                System.out.println("Received message from client: " + message);
                // Broadcast the received message to all clients
                broadcastMessage(message);
            }

            // Client disconnected
            System.out.println("Client disconnected: " + clientSocket);
            clientSocket.close();
            clients.remove(this);
        } catch (IOException e) {
            // Client disconnected unexpectedly
            System.out.println("Client disconnected unexpectedly: " + clientSocket);
            clients.remove(this);
        }
    }
}
