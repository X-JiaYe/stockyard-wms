package com.wms.system.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JWT 单测：jti（黑名单键）与剩余 TTL 的签发/解析行为。
 */
class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "wmsTestJwtSecretKey_0123456789_abcdefghijklmnopqrstuvwxyz");
        ReflectionTestUtils.setField(jwtUtil, "expireSeconds", 7200L);
    }

    @Test
    @DisplayName("签发 token 携带 jti 且可解析用户名")
    void generateToken_hasJtiAndSubject() {
        String token = jwtUtil.generateToken("admin");

        assertThat(jwtUtil.getUsername(token)).isEqualTo("admin");
        assertThat(jwtUtil.getJti(token)).isNotBlank();
    }

    @Test
    @DisplayName("token 剩余有效秒数大于 0")
    void remainingTtl_isPositive() {
        String token = jwtUtil.generateToken("admin");
        assertThat(jwtUtil.getRemainingTtlSeconds(token)).isGreaterThan(0);
    }

    @Test
    @DisplayName("非法 token 解析返回 null（黑名单键为空，放行）")
    void invalidToken_returnsNull() {
        assertThat(jwtUtil.getUsername("not-a-jwt")).isNull();
        assertThat(jwtUtil.getJti("not-a-jwt")).isNull();
        assertThat(jwtUtil.getRemainingTtlSeconds("not-a-jwt")).isZero();
    }
}
