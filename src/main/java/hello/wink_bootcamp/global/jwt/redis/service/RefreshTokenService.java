package hello.wink_bootcamp.global.jwt.redis.service;

import hello.wink_bootcamp.global.jwt.redis.repository.RedisRefreshTokenRepository;
import hello.wink_bootcamp.global.jwt.redis.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;


    @Transactional
    public void saveRefreshToken(Long userId, String refreshToken, Long expiryInSeconds) {
        try {
            refreshTokenRepository.save(userId, refreshToken, expiryInSeconds);

            log.debug("사용자 {}의 리프레시 토큰이 저장되었습니다. 만료 시간: {}초", userId, expiryInSeconds);
        } catch (Exception e) {
            log.error("리프레시 토큰 저장 중 오류 발생: userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("리프레시 토큰 저장에 실패했습니다.", e);
        }
    }

    /**
     * 사용자 ID로 리프레시 토큰 조회
     */
    public Optional<String> findByUserId(Long userId) {
        try {
            return refreshTokenRepository.findByUserId(userId);
        } catch (Exception e) {
            log.error("리프레시 토큰 조회 중 오류 발생: userId={}, error={}", userId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 리프레시 토큰으로 사용자 ID 조회
     */
    public Optional<Long> findUserIdByToken(String refreshToken) {
        try {
            return refreshTokenRepository.findUserIdByToken(refreshToken);
        } catch (Exception e) {
            log.error("사용자 ID 조회 중 오류 발생: error={}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 리프레시 토큰 유효성 검증
     */
    public boolean isValidRefreshToken(Long userId, String refreshToken) {
        try {
            Optional<String> storedToken = findByUserId(userId);
            return storedToken.isPresent() && storedToken.get().equals(refreshToken);
        } catch (Exception e) {
            log.error("리프레시 토큰 검증 중 오류 발생: userId={}, error={}", userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 특정 리프레시 토큰 삭제
     */
    @Transactional
    public void deleteRefreshToken(String refreshToken) {
        try {
            refreshTokenRepository.deleteByToken(refreshToken);
            log.debug("리프레시 토큰이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("리프레시 토큰 삭제 중 오류 발생: error={}", e.getMessage(), e);
        }
    }

    /**
     * 사용자의 모든 리프레시 토큰 삭제
     */
    @Transactional
    public void deleteAllByUserId(Long userId) {
        try {
            refreshTokenRepository.deleteAllByUserId(userId);
            log.debug("사용자 {}의 모든 리프레시 토큰이 삭제되었습니다.", userId);
        } catch (Exception e) {
            log.error("사용자 리프레시 토큰 삭제 중 오류 발생: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    /**
     * 만료된 리프레시 토큰들 정리
     */
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            int deletedCount = refreshTokenRepository.deleteExpiredTokens();
            if (deletedCount > 0) {
                log.info("만료된 리프레시 토큰 {}개가 정리되었습니다.", deletedCount);
            }
        } catch (Exception e) {
            log.error("만료된 토큰 정리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}