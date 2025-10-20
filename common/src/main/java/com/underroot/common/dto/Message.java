package com.underroot.common.dto;

import com.google.gson.Gson;

public record Message(MessageType type, com.google.gson.JsonElement payload) {

    private static final Gson gson = new Gson();

    public <T> T getPayload(Class<T> clazz) {
        return gson.fromJson(payload, (Class<T>) clazz);
    }

    public static Message of(MessageType type, Object payload) {
        return new Message(type, gson.toJsonTree(payload));
    }
}
