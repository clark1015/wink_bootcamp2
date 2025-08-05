package hello.wink_bootcamp.global.jwt.redis.repository;

import java.util.Optional;

public interface RefreshTokenRepository {

    /**
     * 리프레시 토큰 저장
     */
    void save(Long userId, String refreshToken, long expiryInSeconds);

    /**
     * 사용자 ID로 리프레시 토큰 조회
     */
    Optional<String> findByUserId(Long userId);

    /**
     * 리프레시 토큰으로 사용자 ID 조회
     */
    Optional<Long> findUserIdByToken(String refreshToken);

    /**
     * 특정 토큰 삭제
     */
    void deleteByToken(String refreshToken);

    /**
     * 사용자의 모든 토큰 삭제
     */
    void deleteAllByUserId(Long userId);

    /**
     * 만료된 토큰들 삭제
     */
    int deleteExpiredTokens();

    /**
     * 토큰 존재 여부 확인
     */
    boolean existsByUserId(Long userId);
}

