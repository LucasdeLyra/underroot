package com.underroot.common.dto.payload;

public record DeleteTextPayload(String docId, int position, int length) {}
