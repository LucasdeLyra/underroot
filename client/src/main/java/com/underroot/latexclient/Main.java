package com.underroot.latexclient;

import com.underroot.common.dto.Message;
import com.underroot.common.dto.MessageType;
import com.underroot.common.dto.payload.JoinDocumentPayload;
import com.underroot.latexclient.network.ServerConnection;

import javax.swing.*;
import java.awt.GridLayout;

public class Main {
    public static void main(String[] args) {
        // Use SwingUtilities.invokeLater to ensure GUI updates are on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Create a panel with all the input fields
            JTextField serverIpField = new JTextField("127.0.0.1"); // NOVO CAMPO
            JTextField usernameField = new JTextField();
            JTextField docIdField = new JTextField("default-doc");
            JPasswordField passwordField = new JPasswordField();

            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Endereço IP do Servidor:")); // NOVO LABEL
            panel.add(serverIpField); // NOVO CAMPO ADICIONADO
            panel.add(new JLabel("Your Username:"));
            panel.add(usernameField);
            panel.add(new JLabel("Project Name:"));
            panel.add(docIdField);
            panel.add(new JLabel("Project Password:"));
            panel.add(passwordField);

            int result = JOptionPane.showConfirmDialog(null, panel, "Join or Create Project",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String serverIp = serverIpField.getText(); // OBTER O IP
                String username = usernameField.getText();
                String docId = docIdField.getText();
                String password = new String(passwordField.getPassword());

                if (username == null || username.trim().isEmpty()) {
                    username = "user-" + (int) (Math.random() * 1000); // Random username as fallback
                }
                if (docId == null || docId.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Project name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
                if (serverIp == null || serverIp.trim().isEmpty()) {
                    serverIp = "127.0.0.1"; // Fallback para localhost
                }

                // 1. Create the GUI
                LatexEditorGui gui = new LatexEditorGui();

                // 2. Create the server connection (PASSANDO O IP)
                ServerConnection serverConnection = new ServerConnection(gui, serverIp);

                // 3. Link the GUI to the connection and set document details
                gui.setServerConnection(serverConnection);
                gui.setDocId(docId);

                // 4. Connect to the server
                serverConnection.connect();

                // 5. Show the GUI
                gui.setVisible(true);

                // 6. Join the document
                JoinDocumentPayload payload = new JoinDocumentPayload(docId, username, password);
                serverConnection.sendMessage(Message.of(MessageType.JOIN_DOCUMENT, payload));
            } else {
                System.exit(0); // Exit if user cancels
            }
        });
    }
}