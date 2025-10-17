package com.underroot.common.dto.payload;

public record UpdateDocumentPayload(String docId, String operation, int position, String text, int length) {}
