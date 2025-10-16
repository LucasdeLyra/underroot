package com.underroot.latexserver.dto;

// mensagens trocadas pelo cliente e servidor
public record Message(String type, String payload) {
    // type pode ser "JOIN_DOCUMENT", "INSERT_TEXT", etc
    // payload será uma string JSON contendo os dados específicos para aquele tipo de mensagem
}