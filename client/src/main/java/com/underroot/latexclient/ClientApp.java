package com.underroot.latexclient;

import com.underroot.latexclient.dto.Message;
import com.underroot.latexclient.network.ServerConnection;

public class ClientApp {
    public static void main(String[] args) {
        // Inicia a conexão com o servidor
        ServerConnection connection = new ServerConnection();
        connection.connect();

        // Cria uma mensagem de teste (em formato JSON, como esperado pelo servidor)
        // O payload pode ser um JSON representando dados mais complexos
        Message testMessage = new Message("JOIN_DOCUMENT", "{\"docId\": \"123\", \"user\": \"TestUser\"}");

        // Envia a mensagem
        System.out.println("Enviando mensagem para o servidor...");
        connection.sendMessage(testMessage);

        // Mantém a aplicação rodando por um tempo para receber respostas
        try {
            Thread.sleep(5000); // Dorme por 5 segundos
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Desconecta
        connection.disconnect();
    }
}