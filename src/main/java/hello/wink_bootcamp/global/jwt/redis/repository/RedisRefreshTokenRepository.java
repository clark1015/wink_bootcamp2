package hello.wink_bootcamp.global.jwt.redis.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RedisRefreshTokenRepository implements RefreshTokenRepository {

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:user:";
    private static final String TOKEN_TO_USER_PREFIX = "token_to_user:";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void save(Long userId, String refreshToken, long expiryInSeconds) {
        String userKey = REFRESH_TOKEN_PREFIX + userId;
        String tokenKey = TOKEN_TO_USER_PREFIX + refreshToken;

        // 사용자 ID -> 토큰 매핑
        redisTemplate.opsForValue().set(userKey, refreshToken, Duration.ofSeconds(expiryInSeconds));

        // 토큰 -> 사용자 ID 매핑 (토큰으로 사용자 찾기용)
        redisTemplate.opsForValue().set(tokenKey, userId.toString(), Duration.ofSeconds(expiryInSeconds));
    }

    @Override
    public Optional<String> findByUserId(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        String token = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(token);
    }

    @Override
    public Optional<Long> findUserIdByToken(String refreshToken) {
        String key = TOKEN_TO_USER_PREFIX + refreshToken;
        String userId = redisTemplate.opsForValue().get(key);
        return userId != null ? Optional.of(Long.parseLong(userId)) : Optional.empty();
    }

    @Override
    public void deleteByToken(String refreshToken) {
        // 토큰으로 사용자 ID 찾기
        Optional<Long> userId = findUserIdByToken(refreshToken);

        if (userId.isPresent()) {
            String userKey = REFRESH_TOKEN_PREFIX + userId.get();
            redisTemplate.delete(userKey);
        }

        String tokenKey = TOKEN_TO_USER_PREFIX + refreshToken;
        redisTemplate.delete(tokenKey);
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        // 기존 토큰 찾아서 삭제
        Optional<String> existingToken = findByUserId(userId);
        if (existingToken.isPresent()) {
            String tokenKey = TOKEN_TO_USER_PREFIX + existingToken.get();
            redisTemplate.delete(tokenKey);
        }

        String userKey = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.delete(userKey);
    }

    @Override
    public int deleteExpiredTokens() {
        // Redis의 TTL 기능으로 자동 만료되므로 별도 정리 불필요
        // 필요시 SCAN으로 만료된 키들을 찾아 정리
        return 0;
    }

    @Override
    public boolean existsByUserId(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}