package com.underroot.latexserver.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DocumentManagerTest {

    private DocumentManager documentManager;

    @BeforeEach
    void setUp() {
        documentManager = new DocumentManager();
    }

    @Test
    void getOrCreateDocument_ShouldCreateNewDocument_WhenItDoesNotExist() {
        String docId = "new-doc";
        assertNull(documentManager.getDocument(docId), "Document should not exist before creation.");
        
        Document doc = documentManager.getOrCreateDocument(docId);
        
        assertNotNull(doc, "getOrCreateDocument should return a non-null document.");
        assertEquals(docId, doc.getId(), "The new document should have the correct ID.");
        
        Document retrievedDoc = documentManager.getDocument(docId);
        assertSame(doc, retrievedDoc, "Getting the document again should return the same instance.");
    }

    @Test
    void getOrCreateDocument_ShouldReturnExistingDocument_WhenItExists() {
        String docId = "existing-doc";
        Document firstInstance = documentManager.getOrCreateDocument(docId);
        assertNotNull(firstInstance, "First instance should not be null.");

        Document secondInstance = documentManager.getOrCreateDocument(docId);
        
        assertSame(firstInstance, secondInstance, "getOrCreateDocument should return the same instance for the same ID.");
    }

    @Test
    void getDocument_ShouldReturnNull_ForNonExistentDocument() {
        assertNull(documentManager.getDocument("non-existent-doc"), "getDocument should return null for a document that does not exist.");
    }
}
