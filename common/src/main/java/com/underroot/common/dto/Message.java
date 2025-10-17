package com.underroot.common.dto;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public record Message(String type, JsonElement payload) {

    private static final Gson gson = new Gson();

    public <T> T getPayload(Class<T> clazz) {
        return gson.fromJson(payload, clazz);
    }

    public static Message of(String type, Object payload) {
        return new Message(type, gson.toJsonTree(payload));
    }
}
