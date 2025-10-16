package com.underroot.latexserver;

import com.google.gson.Gson;
import com.underroot.latexserver.dto.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson = new Gson(); // JSON parser

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String jsonMessage;
            while ((jsonMessage = in.readLine()) != null) {
                // Deserialize the incoming JSON string into our Message object
                Message message = gson.fromJson(jsonMessage, Message.class);
                handleMessage(message);
            }
        } catch (SocketException e) {
            System.out.println("Client " + clientSocket.getInetAddress().getHostAddress() + " disconnected.");
        } catch (IOException e) {
            System.err.println("Error handling client: " + clientSocket.getInetAddress().getHostAddress());
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    /**
     * Routes incoming messages based on their type.
     */
    private void handleMessage(Message message) {
        System.out.println("Received message of type: " + message.type());
        // This switch statement is where we will add all future message handling logic.
        switch (message.type()) {
            case "JOIN_DOCUMENT":
                // Logic for joining a document will go here
                break;
            case "INSERT_TEXT":
                // Logic for inserting text will go here
                break;
            case "REQUEST_COMPILE":
                // Logic for compiling the document will go here
                break;
            default:
                System.out.println("Unknown message type: " + message.type());
                break;
        }
    }

    // This method can be used by other parts of the server to send a message to this client
    public void sendMessage(Message message) {
        String jsonMessage = gson.toJson(message);
        out.println(jsonMessage);
    }

    /**
     * Helper method to close the connection and its streams safely.
     */
    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}