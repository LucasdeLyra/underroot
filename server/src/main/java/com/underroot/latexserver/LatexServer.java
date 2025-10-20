package com.underroot.latexserver;

import com.underroot.latexserver.model.DocumentManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class LatexServer {

    private static final int PORT = 58008;
    private final DocumentManager documentManager = new DocumentManager();

    // cria um serverSocket na porta 58008
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado. Pegando a porta " + PORT);

            // o loop infinito faz o servidor rodar pra sempre e aceitar novas conexões.
            // para cada cliente novo, vai ser criado um gerente para ele (classe ClientHandle)
            // que será inicializado em uma nova thread
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo cliente conectado: " + clientSocket.getInetAddress().getHostAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket, documentManager);

                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Não foi possível começar o server na porta " + PORT);
            e.printStackTrace();
        }
    }
}