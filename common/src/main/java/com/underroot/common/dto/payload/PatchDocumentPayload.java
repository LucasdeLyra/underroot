package com.underroot.common.dto.payload;

public record PatchDocumentPayload(String docId, String patch, int serverVersion, int clientVersion) {
}
