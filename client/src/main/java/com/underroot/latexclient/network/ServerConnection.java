package com.underroot.latexclient.network;

import com.google.gson.Gson;
import com.underroot.common.dto.Message;
import com.underroot.common.dto.MessageType;
import com.underroot.common.dto.payload.CompileResultPayload;
import com.underroot.common.dto.payload.DocumentStatePayload;
import com.underroot.common.dto.payload.PatchDocumentPayload;
import com.underroot.common.dto.payload.UserJoinedPayload;
import com.underroot.common.dto.payload.UserLeftPayload;
import com.underroot.common.ot.DocumentTransformer;
import com.underroot.latexclient.LatexEditorGui;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class ServerConnection {

    private static final String SERVER_ADDRESS = "127.0.0.1"; // localhost
    private static final int SERVER_PORT = 58008;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson = new Gson(); // JSON parser
    private final LatexEditorGui gui;
    private final DocumentTransformer documentTransformer = new DocumentTransformer();

    public ServerConnection(LatexEditorGui gui) {
        this.gui = gui;
    }

    // tenta se conectar com o servidor e inicia uma nova thread pra ouvir as mensagens do servidor
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

    // metodo para o resto da aplicação cliente usar para enviar mensagens
    public void sendMessage(Message message) {
        if (out != null) {
            String jsonMessage = gson.toJson(message);
            out.println(jsonMessage);
        }
    }

    // esse metodo tem um loop infinito que espera por mensagens do servidor
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

    // interpreta a mensagem que o cliente ouviu do servidor e decide o que fazer
    private void handleMessage(Message message) {
        System.out.println("Received message of type: " + message.type());
        // Use SwingUtilities.invokeLater to ensure GUI updates are on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            switch (message.type()) {
                case MessageType.DOCUMENT_STATE:
                    handleDocumentState(message.getPayload(DocumentStatePayload.class));
                    break;
                case MessageType.PATCH_DOCUMENT:
                    handlePatchDocument(message.getPayload(PatchDocumentPayload.class));
                    break;
                case MessageType.USER_JOINED:
                    handleUserJoined(message.getPayload(UserJoinedPayload.class));
                    break;
                case MessageType.USER_LEFT:
                    handleUserLeft(message.getPayload(UserLeftPayload.class));
                    break;
                case MessageType.COMPILE_RESULT:
                    handleCompileResult(message.getPayload(CompileResultPayload.class));
                    break;
                default:
                    System.out.println("Unknown message type from server: " + message.type());
                    break;
            }
        });
    }

    private void handleDocumentState(DocumentStatePayload payload) {
        gui.setDocumentState(payload.content(), 0); // Initial version is 0
    }

    private void handlePatchDocument(PatchDocumentPayload payload) {
        // This is a simplified OT. A real implementation would need transformation logic.
        String currentText = gui.getTextArea().getText();
        String newText = documentTransformer.applyPatch(currentText, payload.patch());

        gui.getIsRemoteChange().set(true);
        // Preserve cursor position
        int cursorPosition = gui.getTextArea().getCaretPosition();
        gui.getTextArea().setText(newText);
        // Try to restore cursor position, bounded by the new text length
        gui.getTextArea().setCaretPosition(Math.min(cursorPosition, newText.length()));
        gui.getIsRemoteChange().set(false);

        // Update the local state after applying the patch
        gui.setDocumentState(newText, payload.serverVersion());
    }

    private void handleUserJoined(UserJoinedPayload payload) {
        DefaultListModel<String> model = (DefaultListModel<String>) gui.getUserList().getModel();
        if (!model.contains(payload.username())) {
            model.addElement(payload.username());
        }
    }

    private void handleUserLeft(UserLeftPayload payload) {
        DefaultListModel<String> model = (DefaultListModel<String>) gui.getUserList().getModel();
        model.removeElement(payload.username());
    }

    private void handleCompileResult(CompileResultPayload payload) {
        if (payload.success()) {
            try {
                Path tempPdf = Files.createTempFile(payload.docId() + "-", ".pdf");
                Files.write(tempPdf, payload.pdfBytes());
                JOptionPane.showMessageDialog(gui, "Compilation successful! PDF saved to:\n" + tempPdf.toAbsolutePath(), "Compilation Success", JOptionPane.INFORMATION_MESSAGE);
                // Try to open the PDF file
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(tempPdf.toFile());
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(gui, "Compilation successful, but failed to save or open the PDF.", "File Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // Show the error log in a scrollable text area
            JTextArea logArea = new JTextArea(payload.log());
            logArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(logArea);
            scrollPane.setPreferredSize(new Dimension(600, 400));
            JOptionPane.showMessageDialog(gui, scrollPane, "Compilation Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // fecha a conexão
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