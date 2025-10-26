package com.underroot.latexserver;
//  ------ GERADO PARCIALMENTE PELO GEMINI ------
/* Remodelagem para desacoplação dos handlers feita manualmente.
 * Tratamento de erros, melhoria nos logs e feedback ao client feito manualmente.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import com.google.gson.Gson;
import com.underroot.common.dto.Message;
import com.underroot.common.dto.MessageType;
import com.underroot.common.dto.payload.JoinDocumentPayload;
import com.underroot.common.dto.payload.PatchDocumentPayload;
import com.underroot.common.dto.payload.RequestCompilePayload;
import com.underroot.common.dto.payload.UserLeftPayload;
import com.underroot.common.ot.DocumentTransformer;
import com.underroot.latexserver.compiler.LatexCompiler;
import com.underroot.latexserver.handlers.MessageHandler;
import com.underroot.latexserver.model.Document;
import com.underroot.latexserver.model.DocumentManager;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final DocumentManager documentManager;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson = new Gson();
    private String username;
    private Document currentDocument;
    private final LatexCompiler latexCompiler = new LatexCompiler();
    private final DocumentTransformer documentTransformer = new DocumentTransformer();

    public ClientHandler(Socket socket, DocumentManager documentManager) {
        this.clientSocket = socket;
        this.documentManager = documentManager;
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
                // DDesserializa a string JSON recebida em nosso objeto Message.
                Message message = gson.fromJson(jsonMessage, Message.class);
                handleMessage(message);
            }
        } catch (SocketException e) {
            System.out.println("Cliente " + clientSocket.getInetAddress().getHostAddress() + " disconectado.");
        } catch (IOException e) {
            System.err.println("Erro ao lidar com o cliente.: " + clientSocket.getInetAddress().getHostAddress());
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    // direciona as mensagens recebidas com base no seu tipo
    private void handleMessage(Message message) {
        System.out.println("Tipo de mensagem recebida : " + message.type());
//fala tipo da mensagem, mas não de quem
        switch (message.type()) {
            case MessageType.JOIN_DOCUMENT:
                MessageHandler.handleJoinDocument(this, message.getPayload(JoinDocumentPayload.class));
                break;
            case MessageType.PATCH_DOCUMENT:
                MessageHandler.handlePatchDocument(this, message.getPayload(PatchDocumentPayload.class));
                break;
            case MessageType.REQUEST_COMPILE:
                MessageHandler.handleRequestCompile(this, message.getPayload(RequestCompilePayload.class));
                break;
            default:
                System.out.println("Tipo de mensagem desconhecida: " + message.type());
                break;
        }
    }

    // esse metodo pode ser usado por outras partes do servidor para enviar uma mensagem a este cliente
    public void sendMessage(Message message) {
        String jsonMessage = gson.toJson(message);
        out.println(jsonMessage);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Document getCurrentDocument() {
        return currentDocument;
    }

    public void setCurrentDocument(Document currentDocument) {
        this.currentDocument = currentDocument;
    }

    public DocumentManager getDocumentManager() {
        return documentManager;
    }

    public DocumentTransformer getDocumentTransformer() {
        return documentTransformer;
    }

    public LatexCompiler getLatexCompiler() {
        return latexCompiler;
    }

    // fecha a conexão
    private void closeConnection() {
        if (this.currentDocument != null) {
            this.currentDocument.removeCollaborator(this);
            var leftPayload = new UserLeftPayload(this.currentDocument.getId(), this.username);
            this.currentDocument.broadcast(Message.of(MessageType.USER_LEFT, leftPayload), this);
        }
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}