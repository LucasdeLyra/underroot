package com.underroot.common.dto.payload;

public record DocumentStatePayload(String docId, String content, java.util.List<String> collaborators) {
}
