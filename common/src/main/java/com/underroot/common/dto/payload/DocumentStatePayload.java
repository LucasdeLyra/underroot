package com.underroot.common.dto.payload;
import java.util.List;
// Implementação da lista de colaboradores foi feita manualmente List<String> collaborators
public record DocumentStatePayload(String docId, String content, List<String> collaborators) {
}
