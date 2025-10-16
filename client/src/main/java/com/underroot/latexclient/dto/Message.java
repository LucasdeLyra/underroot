package com.underroot.latexclient.dto; // or latexclient.dto on the client side

// A record is a concise way to create an immutable data carrier class.
public record Message(String type, String payload) {
    // 'type' could be "JOIN_DOCUMENT", "INSERT_TEXT", etc.
    // 'payload' will be a JSON string containing the specific data for that message type.
}