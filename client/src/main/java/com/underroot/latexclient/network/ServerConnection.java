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
import com.underroot.latexclient.Main;

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
    private final Gson gson = new Gson();
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

            System.out.println("Conectado ao server.");

            new Thread(this::startListening).start();

        } catch (IOException e) {
            System.err.println("Falha de conexão: " + e.getMessage());
        }
    }

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
            System.out.println("Conexão ao server perdida.");
        }
    }

    // interpreta a mensagem que o cliente ouviu do servidor e decide o que fazer
    private void handleMessage(Message message) {
        System.out.println("Tipo de mensagem recebida: " + message.type());
        // Use SwingUtilities.invokeLater para garantir que as atualizações da interface gráfica (GUI) ocorram na Thread de Despacho de Eventos.
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
                    System.out.println("Tipo de mensagem desconhecida do server: " + message.type());
                    break;
            }
        });
    }

    private void handleDocumentState(DocumentStatePayload payload) {
        gui.setDocumentState(payload.content(), 0); // Initial version is 0
        DefaultListModel<String> model = (DefaultListModel<String>) gui.getUserList().getModel();
        model.clear();
        // O servidor envia a lista de usuários que já estão no documento.
        // A mensagem USER_JOINED adicionará o usuário atual à própria lista.
        for (String user : payload.collaborators()) {
            if (!model.contains(user)) {
                model.addElement(user);
            }
        }
    }

    private void handlePatchDocument(PatchDocumentPayload payload) {
        String currentText = gui.getTextArea().getText();
        String newText = documentTransformer.applyPatch(currentText, payload.patch());

        gui.getIsRemoteChange().set(true);
        int cursorPosition = gui.getTextArea().getCaretPosition();
        gui.getTextArea().setText(newText);

        gui.getTextArea().setCaretPosition(Math.min(cursorPosition, newText.length()));
        gui.getIsRemoteChange().set(false);

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
                JOptionPane.showMessageDialog(gui, "Compilação feita! PDF salvo em:\n" + tempPdf.toAbsolutePath(), "Compilação bem-sucedida", JOptionPane.INFORMATION_MESSAGE);
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(tempPdf.toFile());
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(gui, "Compilação feita, mas falhou para salvar ou abrir o PDF.", "Falha de arquivo", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JTextArea logArea = new JTextArea(payload.log());
            logArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(logArea);
            scrollPane.setPreferredSize(new Dimension(600, 400));
            JOptionPane.showMessageDialog(gui, scrollPane, "Compilação falhou", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAuthFailed() {
        JOptionPane.showMessageDialog(gui, "Autenticação falou. Por favor confira o nome do projeto e a senha.", "Erro de login", JOptionPane.ERROR_MESSAGE);
        disconnect();
        gui.dispose();
        Main.main(new String[]{});
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close(); // Also closes the associated streams
                System.out.println("Disconectado ao servr.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}