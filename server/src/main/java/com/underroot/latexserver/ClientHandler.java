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

    // loop pra ler as mensagens em json que o cliente envia e converter para objeto java do tipo Message
    // o metodo handleMessage é chamado para decidir o que fazer com a mensagem
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

    // direciona as mensagens recebidas com base no seu tipo
    private void handleMessage(Message message) {
        System.out.println("Received message of type: " + message.type());

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

    // esse metodo pode ser usado por outras partes do servidor para enviar uma mensagem a este cliente
    public void sendMessage(Message message) {
        String jsonMessage = gson.toJson(message);
        out.println(jsonMessage);
    }

    // fecha a conexão
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