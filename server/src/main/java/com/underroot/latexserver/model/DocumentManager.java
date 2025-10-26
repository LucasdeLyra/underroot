package com.underroot.latexserver.model;
//  ------ GERADO TOTALMENTE PELO GEMINI ------

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DocumentManager {

    private final Map<String, Document> activeDocuments = new ConcurrentHashMap<>();

    public Document getOrCreateDocument(String docId, String password) {
        return activeDocuments.computeIfAbsent(docId, id -> new Document(id, password));
    }

    public Document getDocument(String docId) {
        return activeDocuments.get(docId);
    }
}
