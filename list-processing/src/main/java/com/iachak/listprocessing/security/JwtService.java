package com.iachak.listprocessing.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private long expiration;

    public String generate(UserDetails ud) {
        return Jwts.builder()
                .subject(ud.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key()).compact();
    }

    public boolean isValid(String token, UserDetails ud) {
        try {
            return extractUsername(token).equals(ud.getUsername()) && !isExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return claim(token, Claims::getSubject);
    }

    private boolean isExpired(String token) {
        return claim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T claim(String token, Function<Claims, T> fn) {
        return fn.apply(Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token).getPayload());
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}
