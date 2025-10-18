package com.underroot.latexserver.model;

import com.underroot.latexserver.ClientHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Document {

    private final String id;
    private String password; // Can be null if no password is set
    private final StringBuilder content;
    private final Set<ClientHandler> collaborators;
    private int version = 0;

    public Document(String id, String password) {
        this.id = id;
        this.password = password;
        this.content = new StringBuilder();
        this.collaborators = ConcurrentHashMap.newKeySet();
    }

    public boolean checkPassword(String password) {
        // If the document's password is null or empty, any password is valid (or no password).
        if (this.password == null || this.password.isEmpty()) {
            return true;
        }
        return this.password.equals(password);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        // Only allow setting the password if one isn't already set.
        if (this.password == null || this.password.isEmpty()) {
            this.password = password;
        }
    }

    public synchronized int getVersion() {
        return version;
    }

    public synchronized void incrementVersion() {
        version++;
    }

    public String getId() {
        return id;
    }


    public String getContent() {
        return content.toString();
    }

    public synchronized void setContent(String newContent) {
        this.content.setLength(0);
        this.content.append(newContent);
    }

    public void addCollaborator(ClientHandler clientHandler) {
        collaborators.add(clientHandler);
    }

    public void removeCollaborator(ClientHandler clientHandler) {
        collaborators.remove(clientHandler);
    }

    public Set<ClientHandler> getCollaborators() {
        return collaborators;
    }

    public synchronized void insertText(int position, String text) {
        content.insert(position, text);
        version++;
    }

    public synchronized void deleteText(int position, int length) {
        content.delete(position, position + length);
        version++;
    }

    public void broadcast(com.underroot.common.dto.Message message, ClientHandler originator) {
        for (ClientHandler collaborator : collaborators) {
            if (originator == null || !collaborator.equals(originator)) {
                collaborator.sendMessage(message);
            }
        }
    }
}
