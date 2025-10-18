package com.underroot.common.dto;

public enum MessageType {
    // Client to Server
    JOIN_DOCUMENT,
    REQUEST_COMPILE,
    PATCH_DOCUMENT,

    // Server to Client
    DOCUMENT_STATE,
    USER_JOINED,
    USER_LEFT,
    COMPILE_RESULT,
    AUTH_FAILED
}
