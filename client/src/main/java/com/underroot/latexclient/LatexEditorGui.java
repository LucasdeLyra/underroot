package com.underroot.latexclient;
//  ------ GERADO PARCIALMENTE PELO GEMINI ------
/* Remoção de imports não utilizados e com .* feitas automaticamente pelo linter.
 * Inserção de campos como endereço de IP, nome de usuário e senha feitos manualmente.
 * Alguns tratamentos de erro como erro de login feitos manualmente.
 */
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.underroot.common.dto.Message;
import com.underroot.common.dto.MessageType;
import com.underroot.common.dto.payload.PatchDocumentPayload;
import com.underroot.common.dto.payload.RequestCompilePayload;
import com.underroot.common.ot.DocumentTransformer;
import com.underroot.latexclient.network.ServerConnection;

public class LatexEditorGui extends JFrame {

    private JTextArea textArea;
    private JList<String> userList;
    private JButton compileButton;
    private JButton logoutButton;
    private ServerConnection serverConnection;
    private final AtomicBoolean isRemoteChange = new AtomicBoolean(false);
    private String docId; // Para saber qual documento a gente está editando
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
        compileButton = new JButton("Compila");
        logoutButton = new JButton("Logout");
    }

    private void layoutComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane textScrollPane = new JScrollPane(textArea);
        mainPanel.add(textScrollPane, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setPreferredSize(new Dimension(200, 0));

        JScrollPane userListScrollPane = new JScrollPane(userList);
        rightPanel.add(userListScrollPane, BorderLayout.CENTER);
        rightPanel.add(new JLabel("Collaboradores"), BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 5)); // Use GridLayout for two buttons
        buttonPanel.add(compileButton);
        buttonPanel.add(logoutButton);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(rightPanel, BorderLayout.EAST);

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
        addLogoutButtonListener();
    }

    public void setDocId(String docId) {
        this.docId = docId;
        // Update the window title to show the project name
        setTitle("Underroot LaTeX Editor - " + docId);
    }

    public void setDocumentState(String content, int version) {
        this.lastKnownText = content;
        this.serverVersion = version;
        // Este pode ser o estado inicial, portanto atualize a área de texto.
        isRemoteChange.set(true);
        textArea.setText(content);
        isRemoteChange.set(false);
    }

    /**
     * Update the internal document state (lastKnownText and serverVersion) without
     * replacing the text in the JTextArea. Use this when the caller already applied
     * the text to the textArea and only wants to synchronize the internal state.
     */
    public void updateLocalDocumentState(String content, int version) {
        this.lastKnownText = content;
        this.serverVersion = version;
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
            }
        });
    }

    private void addCompileButtonListener() {
        compileButton.addActionListener(e -> {
            if (serverConnection != null && docId != null) {
                System.out.println("Botão de compilar clicado para o documento.: " + docId);
                RequestCompilePayload payload = new RequestCompilePayload(docId);
                serverConnection.sendMessage(Message.of(MessageType.REQUEST_COMPILE, payload));
            }
        });
    }

    private void addLogoutButtonListener() {
        logoutButton.addActionListener(e -> {
            if (serverConnection != null) {
                serverConnection.disconnect();
            }
            this.dispose();
            Main.main(new String[]{});
        });
    }

    // Getters para a components para ser acessado de fora
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
