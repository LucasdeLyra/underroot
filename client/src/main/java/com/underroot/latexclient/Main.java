package com.underroot.latexclient;
//  ------ GERADO PARCIALMENTE PELO GEMINI ------
/* fortemente inspirado em https://medium.com/@paritosh_30025/client-server-architecture-from-scratch-java-e5678c0af6c9
 * Remoção de imports não utilizados e com .* feitas automaticamente pelo linter.
 * Alterações como poder aceitar um endereço de IP diferente feitos manualmente.
 */
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.underroot.common.dto.Message;
import com.underroot.common.dto.MessageType;
import com.underroot.common.dto.payload.JoinDocumentPayload;
import com.underroot.latexclient.network.ServerConnection;

public class Main {
    public static void main(String[] args) {
        // Use SwingUtilities.invokeLater para garantir que as atualizações da interface gráfica (GUI) ocorram na Thread de Despacho de Eventos.
        SwingUtilities.invokeLater(() -> {
            JTextField serverIpField = new JTextField("127.0.0.1"); // NOVO CAMPO
            JTextField usernameField = new JTextField();
            JTextField docIdField = new JTextField("default-doc");
            JPasswordField passwordField = new JPasswordField();

            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Endereço IP do Servidor:")); // NOVO LABEL
            panel.add(serverIpField); // NOVO CAMPO ADICIONADO
            panel.add(new JLabel("Seu usuário:"));
            panel.add(usernameField);
            panel.add(new JLabel("Nome do projeto:"));
            panel.add(docIdField);
            panel.add(new JLabel("Senha do projeto:"));
            panel.add(passwordField);

            int result = JOptionPane.showConfirmDialog(null, panel, "Join or Create Project",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String serverIp = serverIpField.getText(); // OBTER O IP
                String username = usernameField.getText();
                String docId = docIdField.getText();
                String password = new String(passwordField.getPassword());

                if (username == null || username.trim().isEmpty()) {
                    username = "usuário-" + (int) (Math.random() * 1000);
                }
                if (docId == null || docId.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Nome do projeto não pode estar vazio.", "Erro", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
                if (serverIp == null || serverIp.trim().isEmpty()) {
                    serverIp = "127.0.0.1"; // Fallback para localhost
                }

                LatexEditorGui gui = new LatexEditorGui();

                ServerConnection serverConnection = new ServerConnection(gui, serverIp);

                gui.setServerConnection(serverConnection);
                gui.setDocId(docId);

                serverConnection.connect();

                gui.setVisible(true);

                JoinDocumentPayload payload = new JoinDocumentPayload(docId, username, password);
                serverConnection.sendMessage(Message.of(MessageType.JOIN_DOCUMENT, payload));
            } else {
                System.exit(0);
            }
        });
    }
}