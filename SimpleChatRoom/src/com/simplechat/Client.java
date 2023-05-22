package com.simplechat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Client extends Application {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String nickname;

    private Thread receiveThread;

    private boolean isConnected;

    private TextArea chatArea;
    private TextField messageField;
    private Button sendButton;
    private TextField nicknameField;
    private Button setNicknameButton;

    @Override
    public void start(Stage primaryStage) {
        try {
            socket = new Socket("localhost", 12345);
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

        chatArea = new TextArea();
        chatArea.setEditable(false);

        messageField = new TextField();
        sendButton = new Button("Send");
        sendButton.setOnAction(event -> sendMessage(messageField.getText().trim()));

        nicknameField = new TextField();
        setNicknameButton = new Button("Set Nickname");
        setNicknameButton.setOnAction(event -> setNickname());

        VBox vBox = new VBox(10, chatArea, messageField, sendButton, nicknameField, setNicknameButton);
        vBox.setPadding(new Insets(10));
        vBox.setPrefSize(400, 300);

        BorderPane root = new BorderPane(vBox);
        BorderPane.setAlignment(vBox, Pos.CENTER);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Simple Chat Client");
        primaryStage.setOnCloseRequest(event -> disconnect());
        primaryStage.show();

        // Start a new thread to listen for messages from the server
        receiveThread = new Thread(() -> {
            try {
                String message;
                while ((message = input.readLine()) != null) {
                    if (message.startsWith("/nickname")) {
                        setNicknameFromServer(message.substring(10));
                    } else {
                        String finalMessage = message;
                        Platform.runLater(() -> chatArea.appendText(finalMessage + "\n"));
                    }
                }
            } catch (IOException e) {
                // Handle the SocketException here when the server is stopped
                if (e instanceof SocketException) {
                    Platform.runLater(() -> chatArea.appendText("Server has stopped. Disconnected from the server.\n"));
                } else {
                    e.printStackTrace();
                }
                this.isConnected = false;
                Platform.runLater(() -> primaryStage.close());
            }
        });

        receiveThread.start();

        // Read user input and send messages to the server
        messageField.setOnAction(event -> sendMessage(messageField.getText().trim()));

        nicknameField.setOnAction(event -> setNickname());

        messageField.requestFocus();
    }

    private void sendMessage(String message) {
        if (!message.isEmpty()) {
            if (nickname != null) {
                output.println(message);
                messageField.clear();
            } else {
                chatArea.appendText("Please set a nickname before sending messages.\n");
            }
        }
    }


    private void setNickname() {
        String newNickname = nicknameField.getText().trim();
        if (!newNickname.isEmpty()) {
            output.println("/nickname " + newNickname);
            nicknameField.clear();
        }
    }

    private void setNicknameFromServer(String newNickname) {
        nickname = newNickname;
        Platform.runLater(() -> chatArea.appendText("Nickname set to: " + nickname + "\n"));
    }

    private void disconnect() {
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
        launch(args);
    }
}
