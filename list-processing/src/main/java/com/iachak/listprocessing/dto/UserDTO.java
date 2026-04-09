package com.iachak.listprocessing.dto;

import com.iachak.listprocessing.entity.User;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record UserDTO(
        UUID id,
        String username,
        String email,
        Set<String> roles
) {
    public static UserDTO from(User u){
        return new UserDTO(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getRoles().stream().map(Enum::name).collect(Collectors.toSet()));
    }
}
