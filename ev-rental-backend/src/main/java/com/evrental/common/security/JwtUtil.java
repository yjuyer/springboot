package com.evrental.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类（适配jjwt 0.11.5）
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 生成Token */
    public String generateToken(Long userId, String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** 从Token获取用户名 */
    public String getUsername(String token) {
        return parse(token).getSubject();
    }

    /** 从Token获取用户ID */
    public Long getUserId(String token) {
        return parse(token).get("userId", Long.class);
    }

    /** 从Token获取角色 */
    public String getRole(String token) {
        return parse(token).get("role", String.class);
    }

    /** 验证Token是否有效 */
    public boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** 解析Token */
    private Claims parse(String token) {
        return Jwts.parser()
                .setSigningKey(getKey())
                .parseClaimsJws(token)
                .getBody();
    }
}
