package com.underroot.latexclient.network;

import com.google.gson.Gson;
import com.underroot.latexclient.dto.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerConnection {

    private static final String SERVER_ADDRESS = "127.0.0.1"; // localhost
    private static final int SERVER_PORT = 58008;

private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson = new Gson(); // JSON parser

    public void connect() {
        try {
            this.socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("✅ Connected to the server.");

            // Start a new thread dedicated to listening for messages from the server.
            new Thread(this::startListening).start();

        } catch (IOException e) {
            System.err.println("❌ Connection failed: " + e.getMessage());
        }
    }

    /**
     * Sends a structured Message object to the server.
     */
    public void sendMessage(Message message) {
        if (out != null) {
            String jsonMessage = gson.toJson(message);
            out.println(jsonMessage);
        }
    }

    /**
     * The main loop for the listener thread.
     */
    private void startListening() {
        try {
            String jsonMessage;
            while ((jsonMessage = in.readLine()) != null) {
                Message message = gson.fromJson(jsonMessage, Message.class);
                handleMessage(message);
            }
        } catch (IOException e) {
            System.out.println("Connection to server lost.");
        }
    }

    /**
     * Routes incoming messages from the server based on their type.
     */
    private void handleMessage(Message message) {
        System.out.println("Received message of type: " + message.type());
        // This is where you'll update the UI based on server messages
        switch (message.type()) {
            case "DOCUMENT_STATE":
            case "UPDATE_DOCUMENT":
                // Logic to update the local editor content will go here
                break;
            case "USER_JOINED":
            case "USER_LEFT":
                // Logic to update the list of collaborators will go here
                break;
            case "COMPILE_RESULT":
                // Logic to show the compiled PDF or an error will go here
                break;
            default:
                System.out.println("Unknown message type from server: " + message.type());
                break;
        }
    }

    /**
     * Closes the connection and all associated resources.
     */
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close(); // Also closes the associated streams
                System.out.println("Disconnected from the server.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}