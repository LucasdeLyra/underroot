package com.underroot.latexserver.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DocumentManager {

    private final Map<String, Document> activeDocuments = new ConcurrentHashMap<>();

    public Document getOrCreateDocument(String docId) {
        return activeDocuments.computeIfAbsent(docId, Document::new);
    }

    public Document getDocument(String docId) {
        return activeDocuments.get(docId);
    }
}
