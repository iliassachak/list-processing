package com.iachak.listprocessing.controller;

import com.iachak.listprocessing.dto.UserDTO;
import com.iachak.listprocessing.entity.Role;
import com.iachak.listprocessing.entity.User;
import com.iachak.listprocessing.exception.InvalidOperationException;
import com.iachak.listprocessing.exception.ResourceNotFoundException;
import com.iachak.listprocessing.repository.UserRepository;
import com.iachak.listprocessing.security.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserManagementController {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    private static final String PROTECTED_USERNAME = "admin";

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> users(){
        return ResponseEntity.ok(userRepo.findAll()
                .stream()
                .map(UserDTO::from)
                .toList());
    }

    // ── Activer / désactiver ─────────────────────────────────────────
    @PatchMapping("/{id}/enabled")
    public ResponseEntity<UserDTO> setEnabled(
            @PathVariable UUID id,
            @RequestBody Map<String, Boolean> body,
            @AuthenticationPrincipal UserDetails ud) {

        User target = findOrThrow(id);
        if (PROTECTED_USERNAME.equals(target.getUsername()))
            throw new InvalidOperationException(
                    "Le compte \"admin\" est protégé et ne peut pas être désactivé.");
        // Un admin ne peut pas se désactiver lui-même
        String caller = ((AppUserDetails) ud).getUser().getUsername();
        if (caller.equals(target.getUsername()))
            throw new InvalidOperationException(
                    "Vous ne pouvez pas désactiver votre propre compte.");

        target.setEnabled(body.get("enabled"));
        return ResponseEntity.ok(UserDTO.from(userRepo.save(target)));
    }

    // ── Ajouter / retirer le rôle ADMIN ─────────────────────────────
    @PatchMapping("/{id}/role-admin")
    public ResponseEntity<UserDTO> setAdminRole(
            @PathVariable UUID id,
            @RequestBody Map<String, Boolean> body,
            @AuthenticationPrincipal UserDetails ud) {

        User target = findOrThrow(id);
        if (PROTECTED_USERNAME.equals(target.getUsername()))
            throw new InvalidOperationException(
                    "Le rôle ADMIN du compte \"admin\" ne peut pas être modifié.");
        // Un admin ne peut pas se retirer son propre rôle
        String caller = ((AppUserDetails) ud).getUser().getUsername();
        if (caller.equals(target.getUsername()) && Boolean.FALSE.equals(body.get("admin")))
            throw new InvalidOperationException(
                    "Vous ne pouvez pas vous retirer votre propre rôle ADMIN.");

        if (Boolean.TRUE.equals(body.get("admin"))) {
            target.getRoles().add(Role.ADMIN);
        } else {
            target.getRoles().remove(Role.ADMIN);
        }
        return ResponseEntity.ok(UserDTO.from(userRepo.save(target)));
    }

    // ── Changer le mot de passe ──────────────────────────────────────
    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {

        String newPassword = body.get("password");
        if (newPassword == null || newPassword.length() < 6)
            throw new InvalidOperationException(
                    "Le mot de passe doit contenir au moins 6 caractères.");
        User target = findOrThrow(id);
        target.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(target);
        return ResponseEntity.noContent().build();
    }

    private User findOrThrow(UUID id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));
    }
}
