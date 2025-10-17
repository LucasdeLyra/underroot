package com.underroot.latexclient;

import com.google.gson.Gson;
import com.underroot.common.dto.Message;
import com.underroot.common.dto.MessageType;
import com.underroot.common.dto.payload.JoinDocumentPayload;
import com.underroot.latexclient.network.ServerConnection;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Use SwingUtilities.invokeLater to ensure GUI updates are on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // In a real app, this would come from user input (e.g., a dialog box)
            String docId = "default-doc";
            String username = "user-" + (int) (Math.random() * 1000); // Random username

            // 1. Create the GUI
            LatexEditorGui gui = new LatexEditorGui();

            // 2. Create the server connection
            ServerConnection serverConnection = new ServerConnection(gui);

            // 3. Link the GUI to the connection and set document details
            gui.setServerConnection(serverConnection);
            gui.setDocId(docId);

            // 4. Connect to the server
            serverConnection.connect();

            // 5. Show the GUI
            gui.setVisible(true);

            // 6. Join the document
            JoinDocumentPayload payload = new JoinDocumentPayload(docId, username);
            Message message = new Message(MessageType.JOIN_DOCUMENT, new Gson().toJsonTree(payload));
            serverConnection.sendMessage(message);
        });
    }
}
