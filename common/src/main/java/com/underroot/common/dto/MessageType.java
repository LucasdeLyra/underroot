package com.underroot.common.dto;

/**
 * Defines the constant types for messages in the protocol.
 * Using constants helps prevent typos and makes the code more maintainable.
 */
public final class MessageType {

    // Client to Server
    public static final String JOIN_DOCUMENT = "JOIN_DOCUMENT";
    public static final String INSERT_TEXT = "INSERT_TEXT";
    public static final String DELETE_TEXT = "DELETE_TEXT";
    public static final String REQUEST_COMPILE = "REQUEST_COMPILE";
    public static final String PATCH_DOCUMENT = "PATCH_DOCUMENT";

    // Server to Client
    public static final String DOCUMENT_STATE = "DOCUMENT_STATE";
    public static final String UPDATE_DOCUMENT = "UPDATE_DOCUMENT";
    public static final String USER_JOINED = "USER_JOINED";
    public static final String USER_LEFT = "USER_LEFT";
    public static final String COMPILE_RESULT = "COMPILE_RESULT";

    private MessageType() {
        // Private constructor to prevent instantiation of this utility class.
    }
}
