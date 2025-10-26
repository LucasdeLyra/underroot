package com.underroot.latexserver;
// fortemente inspirado em https://medium.com/@paritosh_30025/client-server-architecture-from-scratch-java-e5678c0af6c9
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.underroot.latexserver.model.DocumentManager;

public class LatexServer {

    private static final int PORT = 58008;
    private final DocumentManager documentManager = new DocumentManager();

    // cria um serverSocket na porta 58008
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Listening on port " + PORT);

            // o loop infinito faz o servidor rodar pra sempre e aceitar novas conexões.
            // para cada cliente novo, vai ser criado um gerente para ele (classe ClientHandle)
            // que será inicializado em uma nova thread
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                // Create a new ClientHandler for the connected client
                ClientHandler clientHandler = new ClientHandler(clientSocket, documentManager);

                // Start the handler in a new thread
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Could not start server on port " + PORT);
            e.printStackTrace();
        }
    }
}