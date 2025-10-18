package com.underroot.latexclient;

import com.underroot.common.dto.Message;
import com.underroot.common.dto.MessageType;
import com.underroot.common.dto.payload.PatchDocumentPayload;
import com.underroot.common.dto.payload.RequestCompilePayload;
import com.underroot.common.ot.DocumentTransformer;
import com.underroot.latexclient.network.ServerConnection;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class LatexEditorGui extends JFrame {

    private JTextArea textArea;
    private JList<String> userList;
    private JButton compileButton;
    private JButton logoutButton; // Add logout button field
    private ServerConnection serverConnection;
    private final AtomicBoolean isRemoteChange = new AtomicBoolean(false);
    private String docId; // To know which document we are editing
    private final DocumentTransformer documentTransformer = new DocumentTransformer();
    private String lastKnownText = "";
    private int clientVersion = 0;
    private int serverVersion = 0;

    public LatexEditorGui() {
        super("Underroot LaTeX Editor");
        initComponents();
        layoutComponents();
        setupWindow();
    }

    private void initComponents() {
        textArea = new JTextArea();
        userList = new JList<>(new DefaultListModel<>());
        compileButton = new JButton("Compile");
        logoutButton = new JButton("Logout"); // Initialize the button
    }

    private void layoutComponents() {
        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Text area in the center
        JScrollPane textScrollPane = new JScrollPane(textArea);
        mainPanel.add(textScrollPane, BorderLayout.CENTER);

        // Right panel for users and compile button
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setPreferredSize(new Dimension(200, 0));

        // User list
        JScrollPane userListScrollPane = new JScrollPane(userList);
        rightPanel.add(userListScrollPane, BorderLayout.CENTER);
        rightPanel.add(new JLabel("Collaborators"), BorderLayout.NORTH);


        // Compile and Logout buttons at the bottom
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 5)); // Use GridLayout for two buttons
        buttonPanel.add(compileButton);
        buttonPanel.add(logoutButton);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(rightPanel, BorderLayout.EAST);

        // Add main panel to the frame
        setContentPane(mainPanel);
    }

    private void setupWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null); // Center the window
    }

    public void setServerConnection(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
        addTextModificationListener();
        addCompileButtonListener();
        addLogoutButtonListener(); // Add listener for the new button
    }

    public void setDocId(String docId) {
        this.docId = docId;
        // Update the window title to show the project name
        setTitle("Underroot LaTeX Editor - " + docId);
    }

    public void setDocumentState(String content, int version) {
        this.lastKnownText = content;
        this.serverVersion = version;
        // This might be the initial state, so update the text area
        isRemoteChange.set(true);
        textArea.setText(content);
        isRemoteChange.set(false);
    }

    public AtomicBoolean getIsRemoteChange() {
        return isRemoteChange;
    }

    private void addTextModificationListener() {
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            private void handleChange() {
                if (isRemoteChange.get()) return;

                String newText = textArea.getText();
                String patch = documentTransformer.createPatch(lastKnownText, newText);
                lastKnownText = newText;
                clientVersion++;

                PatchDocumentPayload payload = new PatchDocumentPayload(docId, patch, serverVersion, clientVersion);
                serverConnection.sendMessage(Message.of(MessageType.PATCH_DOCUMENT, payload));
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                handleChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                handleChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Plain text components do not fire these events
            }
        });
    }

    private void addCompileButtonListener() {
        compileButton.addActionListener(e -> {
            if (serverConnection != null && docId != null) {
                System.out.println("Compile button clicked for doc: " + docId);
                RequestCompilePayload payload = new RequestCompilePayload(docId);
                serverConnection.sendMessage(Message.of(MessageType.REQUEST_COMPILE, payload));
            }
        });
    }

    private void addLogoutButtonListener() {
        logoutButton.addActionListener(e -> {
            // Disconnect from the server
            if (serverConnection != null) {
                serverConnection.disconnect();
            }
            // Close the current editor window
            this.dispose();
            // Re-run the main method to show the login dialog again
            Main.main(new String[]{});
        });
    }

    // Getters for components to be accessed from outside
    public JTextArea getTextArea() {
        return textArea;
    }

    public JList<String> getUserList() {
        return userList;
    }

    public JButton getCompileButton() {
        return compileButton;
    }
}
