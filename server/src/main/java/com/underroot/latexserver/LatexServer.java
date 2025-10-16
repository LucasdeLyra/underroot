package com.underroot.latexserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class LatexServer {

    private static final int PORT = 58008;

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("✅ Server started. Listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                // Create a new ClientHandler for the connected client
                ClientHandler clientHandler = new ClientHandler(clientSocket);

                // Start the handler in a new thread
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("❌ Could not start server on port " + PORT);
            e.printStackTrace();
        }
    }
}