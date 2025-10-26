package com.underroot.common.dto.payload;
//  ------ GERADO TOTALMENTE PELO GEMINI ------

public record CompileResultPayload(String docId, boolean success, byte[] pdfBytes, String log) {}
