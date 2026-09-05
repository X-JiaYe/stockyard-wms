package com.wms.system.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 工具：签发与解析。
 */
@Component
public class JwtUtil {

    @Value("${wms.jwt.secret}")
    private String secret;

    @Value("${wms.jwt.expire-seconds}")
    private long expireSeconds;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expireSeconds * 1000);
        return Jwts.builder()
                .subject(username)
                .id(UUID.randomUUID().toString())   // jti：唯一标识，登出黑名单用
                .issuedAt(now)
                .expiration(exp)
                .signWith(key())
                .compact();
    }

    /**
     * 解析并校验 token，非法或过期时返回 null。
     */
    public String getUsername(String token) {
        Claims claims = parse(token);
        return claims == null ? null : claims.getSubject();
    }

    /**
     * 取 token 的唯一标识 jti，非法时返回 null（登出黑名单的键）。
     */
    public String getJti(String token) {
        Claims claims = parse(token);
        return claims == null ? null : claims.getId();
    }

    /**
     * 取 token 剩余有效秒数（用于黑名单 TTL，让其随 token 一起自然过期）。
     */
    public long getRemainingTtlSeconds(String token) {
        Claims claims = parse(token);
        if (claims == null || claims.getExpiration() == null) {
            return 0;
        }
        long remainMs = claims.getExpiration().getTime() - System.currentTimeMillis();
        return remainMs <= 0 ? 0 : remainMs / 1000;
    }

    private Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }
}
