package com.iachak.listprocessing.controller;

import com.iachak.listprocessing.dto.AuthRequest;
import com.iachak.listprocessing.dto.AuthResponse;
import com.iachak.listprocessing.dto.RegisterRequest;
import com.iachak.listprocessing.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest r) {
        return ResponseEntity.ok(authService.register(r));
    }
}
