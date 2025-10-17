package com.underroot.common.dto.payload;

public record CompileResultPayload(String docId, boolean success, byte[] pdfBytes, String log) {}
