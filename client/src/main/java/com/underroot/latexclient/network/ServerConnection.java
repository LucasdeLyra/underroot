package com.underroot.latexclient.network;
//  ------ GERADO PARCIALMENTE PELO GEMINI ------
/* Remoção de imports não utilizados e com .* feitas automaticamente pelo linter.
 * Fortemente inspirado em https://medium.com/@paritosh_30025/client-server-architecture-from-scratch-java-e5678c0af6c9
 */
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

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
import com.underroot.latexclient.Main;

public class ServerConnection {

    // private static final String SERVER_ADDRESS = "127.0.0.1"; // REMOVIDO
    private final String serverAddress; // NOVO CAMPO
    private static final int SERVER_PORT = 58008;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson = new Gson(); // JSON parser
    private final LatexEditorGui gui;
    private final DocumentTransformer documentTransformer = new DocumentTransformer();

    // CONSTRUTOR ATUALIZADO
    public ServerConnection(LatexEditorGui gui, String serverAddress) {
        this.gui = gui;
        this.serverAddress = serverAddress;
    }

    // tenta se conectar com o servidor e inicia uma nova thread pra ouvir as mensagens do servidor
    public void connect() {
        try {
            // USA O CAMPO serverAddress EM VEZ DA CONSTANTE
            this.socket = new Socket(this.serverAddress, SERVER_PORT);
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Connected to the server at " + this.serverAddress);

            // Start a new thread dedicated to listening for messages from the server.
            new Thread(this::startListening).start();

        } catch (IOException e) {
            System.err.println("Connection failed to " + this.serverAddress + ": " + e.getMessage());
            JOptionPane.showMessageDialog(gui, "Falha ao conectar ao servidor em:\n" + this.serverAddress + "\nVerifique o IP e se o servidor está rodando.", "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
            // Reinicia o login
            disconnect();
            gui.dispose();
            Main.main(new String[]{});
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
            if (socket != null && !socket.isClosed()) {
                System.out.println("Connection to server lost.");
            }
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
                case MessageType.AUTH_FAILED:
                    handleAuthFailed();
                    break;
                default:
                    System.out.println("Unknown message type from server: " + message.type());
                    break;
            }
        });
    }

    private void handleDocumentState(DocumentStatePayload payload) {
        gui.setDocumentState(payload.content(), 0); // Initial version is 0
        // Populate the user list
        DefaultListModel<String> model = (DefaultListModel<String>) gui.getUserList().getModel();
        model.clear();
        // The server sends the list of users already in the document.
        // The USER_JOINED message will add the current user to their own list.
        for (String user : payload.collaborators()) {
            if (!model.contains(user)) {
                model.addElement(user);
            }
        }
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

    private void handleAuthFailed() {
        JOptionPane.showMessageDialog(gui, "Authentication failed. Please check the project name and password.", "Login Error", JOptionPane.ERROR_MESSAGE);
        // Disconnect and restart the login process
        disconnect();
        gui.dispose();
        Main.main(new String[]{});
    }

    // fecha a conexão
    public void disconnect() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) {
                socket.close(); // Also closes the associated streams
                System.out.println("Disconnected from the server.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}