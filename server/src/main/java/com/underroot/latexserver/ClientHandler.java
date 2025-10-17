package com.underroot.latexserver;

import com.google.gson.Gson;
import com.underroot.common.dto.Message;
import com.underroot.common.dto.MessageType;
import com.underroot.common.dto.payload.DocumentStatePayload;
import com.underroot.common.dto.payload.JoinDocumentPayload;
import com.underroot.common.dto.payload.PatchDocumentPayload;
import com.underroot.common.dto.payload.RequestCompilePayload;
import com.underroot.common.dto.payload.UserJoinedPayload;
import com.underroot.common.dto.payload.UserLeftPayload;
import com.underroot.common.ot.DocumentTransformer;
import com.underroot.latexserver.compiler.LatexCompiler;
import com.underroot.latexserver.model.Document;
import com.underroot.latexserver.model.DocumentManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

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
            // TODO: Gracefully remove client from any documents they were in
            closeConnection();
        }
    }

    // direciona as mensagens recebidas com base no seu tipo
    private void handleMessage(Message message) {
        System.out.println("Received message of type: " + message.type());

        switch (message.type()) {
            case MessageType.JOIN_DOCUMENT:
                handleJoinDocument(message.getPayload(JoinDocumentPayload.class));
                break;
            case MessageType.PATCH_DOCUMENT:
                handlePatchDocument(message.getPayload(PatchDocumentPayload.class));
                break;
            case MessageType.REQUEST_COMPILE:
                handleRequestCompile(message.getPayload(RequestCompilePayload.class));
                break;
            default:
                System.out.println("Unknown message type: " + message.type());
                break;
        }
    }

    private void handleJoinDocument(JoinDocumentPayload payload) {
        this.username = payload.username();
        this.currentDocument = documentManager.getOrCreateDocument(payload.docId());
        this.currentDocument.addCollaborator(this);

        // 1. Send the full document state to the newly joined user
        var statePayload = new DocumentStatePayload(currentDocument.getId(), currentDocument.getContent());
        var stateMessage = new Message(MessageType.DOCUMENT_STATE, gson.toJsonTree(statePayload));
        this.sendMessage(stateMessage);

        // 2. Notify all *other* users that a new user has joined
        var joinedPayload = new UserJoinedPayload(currentDocument.getId(), this.username);
        var joinedMessage = new Message(MessageType.USER_JOINED, gson.toJsonTree(joinedPayload));
        currentDocument.broadcast(joinedMessage, this); // Pass `this` to exclude self
    }

    private void handlePatchDocument(PatchDocumentPayload payload) {
        Document doc = documentManager.getDocument(payload.docId());
        if (doc != null) {
            // In a real OT implementation, we would transform the patch here
            // before applying it. For now, we apply it directly.
            String newContent = documentTransformer.applyPatch(doc.getContent(), payload.patch());
            doc.setContent(newContent);
            doc.incrementVersion();

            // Broadcast the patch to other collaborators
            var broadcastPayload = new PatchDocumentPayload(doc.getId(), payload.patch(), doc.getVersion(), payload.clientVersion() + 1);
            var broadcastMessage = new Message(MessageType.PATCH_DOCUMENT, gson.toJsonTree(broadcastPayload));
            doc.broadcast(broadcastMessage, this);
        }
    }

    private void handleRequestCompile(RequestCompilePayload payload) {
        Document doc = documentManager.getDocument(payload.docId());
        if (doc != null) {
            System.out.println("Compiling document: " + doc.getId());
            LatexCompiler.CompilationResult result = latexCompiler.compile(doc.getId(), doc.getContent());

            // Send the result back to the originating client
            var compileResultPayload = new com.underroot.common.dto.payload.CompileResultPayload(doc.getId(), result.success(), result.fileBytes(), result.log());
            var resultMessage = new Message(MessageType.COMPILE_RESULT, gson.toJsonTree(compileResultPayload));
            this.sendMessage(resultMessage);
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

    // fecha a conexão
    private void closeConnection() {
        if (this.currentDocument != null) {
            this.currentDocument.removeCollaborator(this);
            var leftPayload = new UserLeftPayload(this.currentDocument.getId(), this.username);
            var leftMessage = new Message(MessageType.USER_LEFT, gson.toJsonTree(leftPayload));
            this.currentDocument.broadcast(leftMessage, this);
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