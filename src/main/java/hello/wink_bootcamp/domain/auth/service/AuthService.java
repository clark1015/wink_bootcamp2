package hello.wink_bootcamp.domain.auth.service;

import hello.wink_bootcamp.domain.auth.dto.request.LoginRequest;
import hello.wink_bootcamp.domain.auth.dto.request.RegisterRequest;
import hello.wink_bootcamp.domain.auth.dto.request.TokenRefreshRequest;
import hello.wink_bootcamp.domain.auth.dto.response.LoginResponse;
import hello.wink_bootcamp.domain.auth.dto.response.RegisterResponse;
import hello.wink_bootcamp.domain.auth.dto.response.TokenRefreshResponse;
import hello.wink_bootcamp.domain.auth.exception.AuthException;
import hello.wink_bootcamp.domain.auth.exception.AuthExceptions;
import hello.wink_bootcamp.domain.user.entity.User;
import hello.wink_bootcamp.domain.user.repository.UserRepository;
import hello.wink_bootcamp.global.jwt.JwtProperties;
import hello.wink_bootcamp.global.jwt.TokenProvider;
import hello.wink_bootcamp.global.jwt.redis.service.RefreshTokenService;
import hello.wink_bootcamp.global.jwt.redis.service.TokenBlackListService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final TokenBlackListService tokenBlackListService;
    private final RedisTemplate<String, String> redisTemplate;
    private final RefreshTokenService refreshTokenService;
    private final JwtProperties jwtProperties; // 추가

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> AuthException.of(AuthExceptions.EMAIL_NOT_REGISTERED));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw AuthException.of(AuthExceptions.INVALID_PASSWORD);
        }

        // JwtProperties 사용
        String accessToken = tokenProvider.generateAccessToken(
                user,
                Duration.ofMillis(jwtProperties.getAccessTokenExpiration())
        );
        String newRefreshToken = tokenProvider.generateRefreshToken(
                user,
                Duration.ofMillis(jwtProperties.getRefreshTokenExpiration())
        );

        long expirationInSeconds = tokenProvider.getRemainingExpiration(newRefreshToken);
        refreshTokenService.saveRefreshToken(user.getUserid(), newRefreshToken, expirationInSeconds);

        return LoginResponse.builder()
                .userId(user.getUserid())
                .email(user.getEmail())
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public void logout(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw AuthException.of(AuthExceptions.MISSING_TOKEN);
        }

        String pureToken = token.substring(7); // "Bearer " 제거

        if (!tokenProvider.validateToken(pureToken)) {
            throw AuthException.of(AuthExceptions.INVALID_TOKEN);
        }
        if (tokenBlackListService.isBlacklisted(pureToken)) {
            throw AuthException.of(AuthExceptions.ALREADY_LOGGED_OUT);
        }

        long expirationInSeconds = tokenProvider.getRemainingExpiration(pureToken);
        long userId = tokenProvider.getUserId(pureToken);

        tokenBlackListService.blacklistToken(pureToken, expirationInSeconds);
        refreshTokenService.deleteAllByUserId(userId);
    }

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String EMAIL_VERIFY_KEY_PREFIX = "email:verify:";
    private static final String VERIFIED_STATUS = "verified";

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // 1. 이메일 인증 상태 확인 (EmailAuthService에서 처리)
        validateEmailVerified(request.email());

        // 2. 이메일 중복 확인 (이중 체크)
        validateEmailNotDuplicated(request.email());

        // 3. 사용자명 중복 확인
        validateUsernameNotDuplicated(request.username());

        // 4. 사용자 생성
        User user = createUser(request);
        User savedUser = userRepository.save(user);

        // 5. 인증 완료된 이메일 정보 Redis에서 제거
        cleanupEmailVerification(request.email());

        log.info("회원가입 완료: {} ({})", savedUser.getEmail(), savedUser.getUsername());
        return new RegisterResponse(
                savedUser.getUserid(),
                savedUser.getEmail(),
                savedUser.getUsername(),
                "회원가입이 완료되었습니다."
        );
    }

    private User createUser(RegisterRequest request) {
        return User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .username(request.username())
                .build();
    }

    private void cleanupEmailVerification(String email) {
        String verificationKey = EMAIL_VERIFY_KEY_PREFIX + email;
        redisTemplate.delete(verificationKey);
        log.debug("이메일 인증 정보 정리 완료: {}", email);
    }

    private void validateEmailVerified(String email) {
        String verificationKey = EMAIL_VERIFY_KEY_PREFIX + email;
        String status = redisTemplate.opsForValue().get(verificationKey);

        if (!VERIFIED_STATUS.equals(status)) {
            log.warn("인증되지 않은 이메일로 회원가입 시도: {}", email);
            throw AuthException.of(AuthExceptions.EMAIL_NOT_VERIFIED);
        }
    }

    private void validateEmailNotDuplicated(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("중복된 이메일로 회원가입 시도: {}", email);
            throw AuthException.of(AuthExceptions.EMAIL_ALREADY_EXISTS);
        }
    }

    private void validateUsernameNotDuplicated(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            log.warn("중복된 사용자명으로 회원가입 시도: {}", username);
            throw AuthException.of(AuthExceptions.USERNAME_ALREADY_EXISTS);
        }
    }

    @Transactional
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String refreshToken = request.refreshToken();

        // 리프레시 토큰 유효성 검증 (만료, 서명 등)
        if (!tokenProvider.validateToken(refreshToken)) {
            throw AuthException.of(AuthExceptions.INVALID_TOKEN);
        }

        // 리프레시 토큰인지 확인
        if (!tokenProvider.isRefreshToken(refreshToken)) {
            throw AuthException.of(AuthExceptions.INVALID_TOKEN_TYPE);
        }

        // 사용자 정보 조회
        Long userId = tokenProvider.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> AuthException.of(AuthExceptions.USER_NOT_FOUND));

        // DB에 저장된 리프레시 토큰과 일치하는지 확인 (stateful 검증)
        String storedRefreshToken = refreshTokenService.findByUserId(userId)
                .orElseThrow(() -> AuthException.of(AuthExceptions.REFRESH_TOKEN_NOT_FOUND));

        if (!storedRefreshToken.equals(refreshToken)) {
            throw AuthException.of(AuthExceptions.INVALID_REFRESH_TOKEN);
        }

        // 기존 Access Token 블랙리스트 처리 (요청 헤더에서 가져오기)
        String oldAccessToken = request.accessToken(); // → 이 필드가 존재해야 함
        if (oldAccessToken != null) {
            if (tokenProvider.validateToken(oldAccessToken)) {
                long remainingTime = tokenProvider.getRemainingExpiration(oldAccessToken);
                tokenBlackListService.blacklistToken(oldAccessToken, remainingTime);
            }
        }


        // 새로운 액세스 토큰 생성 (JwtProperties 사용)
        String newAccessToken = tokenProvider.generateAccessToken(
                user,
                Duration.ofMillis(jwtProperties.getAccessTokenExpiration())
        );

        // 새로운 리프레시 토큰 생성 (RTR 방식, JwtProperties 사용)
        String newRefreshToken = tokenProvider.generateRefreshToken(
                user,
                Duration.ofMillis(jwtProperties.getRefreshTokenExpiration())
        );

        // 기존 리프레시 토큰 삭제 및 새 토큰 저장 (DB에서 관리)
        refreshTokenService.deleteAllByUserId(userId);
        long expirationInSeconds = tokenProvider.getRemainingExpiration(newRefreshToken);
        refreshTokenService.saveRefreshToken(userId, newRefreshToken, expirationInSeconds);

        return new TokenRefreshResponse(newAccessToken, newRefreshToken);
    }
}