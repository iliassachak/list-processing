package com.iachak.listprocessing.dto;

import java.util.Set;
import java.util.UUID;

public record AuthResponse(
        String token,
        String username,
        Set<String> roles,
        UUID userId
) {
}
