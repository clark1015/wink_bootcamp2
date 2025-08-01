package hello.wink_bootcamp.global.jwt.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlackListService {
    private final StringRedisTemplate redisTemplate;

    private static final String BLACKLIST_PREFIX = "blacklist:";

   // token이 블랙 리스트에 등록 되었는지 확인(로그아웃된 토큰인지)
    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey(BLACKLIST_PREFIX + token);
    }

    //logout 처리시 redis black list에 등록
    public void blacklistToken(String token, long expirationInSeconds) {
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + token,
                "logout",
                expirationInSeconds,
                TimeUnit.SECONDS
        );
    }
}
