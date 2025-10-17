package com.underroot.latexclient;

import com.underroot.latexclient.dto.Message;
import com.underroot.latexclient.network.ServerConnection;

public class Main {
    public static void main(String[] args) {
        ServerConnection connection = new ServerConnection();

        // The connect() method will start its own background thread for listening
        connection.connect();

        // Wait a moment to ensure the connection is established before sending.
        try {
            Thread.sleep(1000); // 1 second
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Send a test message to the server
        System.out.println("Sending a test JOIN_DOCUMENT message...");
        Message testMessage = new Message("JOIN_DOCUMENT", "{\"docId\": \"sample-doc-123\"}");
        connection.sendMessage(testMessage);
    }
}
