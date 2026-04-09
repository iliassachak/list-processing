package com.iachak.listprocessing.service;

import com.iachak.listprocessing.dto.AuthRequest;
import com.iachak.listprocessing.dto.AuthResponse;
import com.iachak.listprocessing.dto.RegisterRequest;
import com.iachak.listprocessing.entity.Role;
import com.iachak.listprocessing.entity.User;
import com.iachak.listprocessing.repository.UserRepository;
import com.iachak.listprocessing.security.AppUserDetails;
import com.iachak.listprocessing.security.AppUserDetailsService;
import com.iachak.listprocessing.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;
    private final AppUserDetailsService userDetailsService;

    public AuthResponse login(AuthRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );

        var ud = (AppUserDetails) userDetailsService.loadUserByUsername(req.username());
        Set<String> roles = ud.getUser().getRoles()
                .stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
        return new AuthResponse(jwtService.generate(ud), req.username(), roles, ud.getUser().getId());
    }

    public AuthResponse register(RegisterRequest req) {

        if (userRepo.existsByUsername(req.username()))
            throw new IllegalArgumentException("Username already taken");

        User u = new User();
        u.setUsername(req.username());
        u.setEmail(req.email());
        u.setPassword(encoder.encode(req.password()));
        u.setRoles(Set.of(Role.USER));
        userRepo.save(u);
        return login(new AuthRequest(req.username(), req.password()));
    }
}
