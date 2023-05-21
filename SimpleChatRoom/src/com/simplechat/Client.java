package com.simplechat;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;
public class Client
{
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 12345); // Replace with the server's IP address and port number
            System.out.println("Connected to server: " + socket);

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            // Start a new thread to listen for messages from the server
            Thread receiveThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = input.readLine()) != null) {
                        System.out.println("Received message from server: " + message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();

            // Read messages from the console and send them to the server
            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
            String message;
            while ((message = consoleInput.readLine()) != null) {
                output.println(message);
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
