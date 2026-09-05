package com.wms.system.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * JWT 黑名单：无状态 JWT 本身无法主动失效，登出时把 token 的 jti 写入 Redis，
 * 校验时命中黑名单即拒绝，实现「登出立即失效」。TTL 与 token 剩余有效期一致，自动清理。
 *
 * <p>设计取舍：Redis 不可用时「fail-open」（校验放行、登出仅告警），保证核心出入库链路
 * 不因缓存中间件故障而中断；代价是极端情况下登出失效有短暂窗口，可用集群/持久化兜底。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "auth:blacklist:";

    private final StringRedisTemplate redisTemplate;

    public void blacklist(String jti, long ttlSeconds) {
        if (jti == null || jti.isBlank()) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + jti, "1", Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.warn("写入 JWT 黑名单失败（Redis 不可用？），登出仅本地生效: jti={}", jti, e);
        }
    }

    public boolean isBlacklisted(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + jti));
        } catch (Exception e) {
            log.warn("查询 JWT 黑名单失败（Redis 不可用？），按放行处理: jti={}", jti, e);
            return false;
        }
    }
}
