package com.underroot.latexserver.handlers;

import com.underroot.common.dto.Message;
import com.underroot.common.dto.MessageType;
import com.underroot.common.dto.payload.DocumentStatePayload;
import com.underroot.common.dto.payload.JoinDocumentPayload;
import com.underroot.common.dto.payload.PatchDocumentPayload;
import com.underroot.common.dto.payload.RequestCompilePayload;
import com.underroot.common.dto.payload.UserJoinedPayload;
import com.underroot.latexserver.ClientHandler;
import com.underroot.latexserver.compiler.LatexCompiler;
import com.underroot.latexserver.model.Document;

public class MessageHandler {

    public static void handleJoinDocument(ClientHandler client, JoinDocumentPayload payload) {
        // Use getOrCreateDocument which is atomic.
        Document doc = client.getDocumentManager().getOrCreateDocument(payload.docId(), payload.password());

        // Now, check the password.
        if (!doc.checkPassword(payload.password())) {
            System.err.println("Authentication failed for user '" + payload.username() + "' on document '" + payload.docId() + "'.");
            client.sendMessage(Message.of(MessageType.AUTH_FAILED, null));
            return;
        }

        client.setUsername(payload.username());
        client.setCurrentDocument(doc);

        var collaboratorNames = doc.getCollaborators().stream()
                .map(ClientHandler::getUsername)
                .collect(java.util.stream.Collectors.toList());

        doc.addCollaborator(client);

        // 1. Send the full document state to the newly joined user, including existing collaborators
        var statePayload = new DocumentStatePayload(doc.getId(), doc.getContent(), collaboratorNames);
        client.sendMessage(Message.of(MessageType.DOCUMENT_STATE, statePayload));

        // 2. Notify all *other* users that a new user has joined
        var joinedPayload = new UserJoinedPayload(doc.getId(), client.getUsername());
        doc.broadcast(Message.of(MessageType.USER_JOINED, joinedPayload), client);
    }

    public static void handlePatchDocument(ClientHandler client, PatchDocumentPayload payload) {
        Document doc = client.getDocumentManager().getDocument(payload.docId());
        if (doc != null) {
            String newContent = client.getDocumentTransformer().applyPatch(doc.getContent(), payload.patch());
            doc.setContent(newContent);
            doc.incrementVersion();

            var broadcastPayload = new PatchDocumentPayload(doc.getId(), payload.patch(), doc.getVersion(), payload.clientVersion() + 1);
            doc.broadcast(Message.of(MessageType.PATCH_DOCUMENT, broadcastPayload), client);
        }
    }

    public static void handleRequestCompile(ClientHandler client, RequestCompilePayload payload) {
        Document doc = client.getDocumentManager().getDocument(payload.docId());
        if (doc != null) {
            System.out.println("Compiling document: " + doc.getId());
            LatexCompiler.CompilationResult result = client.getLatexCompiler().compile(doc.getId(), doc.getContent());

            var compileResultPayload = new com.underroot.common.dto.payload.CompileResultPayload(doc.getId(), result.success(), result.fileBytes(), result.log());
            client.sendMessage(Message.of(MessageType.COMPILE_RESULT, compileResultPayload));
        }
    }
}
