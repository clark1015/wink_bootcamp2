package hello.wink_bootcamp.domain.auth.service;

import hello.wink_bootcamp.domain.auth.dto.request.LoginRequest;
import hello.wink_bootcamp.domain.auth.dto.request.RegisterRequest;
import hello.wink_bootcamp.domain.auth.dto.response.LoginResponse;
import hello.wink_bootcamp.domain.auth.dto.response.RegisterResponse;
import hello.wink_bootcamp.domain.auth.exception.AuthException;
import hello.wink_bootcamp.domain.auth.exception.AuthExceptions;
import hello.wink_bootcamp.domain.user.entity.User;
import hello.wink_bootcamp.domain.user.repository.UserRepository;
import hello.wink_bootcamp.global.jwt.TokenProvider;
import hello.wink_bootcamp.global.jwt.redis.TokenBlackListService;
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

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> AuthException.of(AuthExceptions.EMAIL_NOT_REGISTERED));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw AuthException.of(AuthExceptions.INVALID_PASSWORD);
        }

        String token = tokenProvider.generateToken(user, Duration.ofHours(2));
        return new LoginResponse(user.getUserid(), user.getEmail(), token);
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

        tokenBlackListService.blacklistToken(pureToken, expirationInSeconds);
    }

    private static final String BEARER_PREFIX = "Bearer ";
    private static final Duration DEFAULT_TOKEN_EXPIRY = Duration.ofHours(2);
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
}