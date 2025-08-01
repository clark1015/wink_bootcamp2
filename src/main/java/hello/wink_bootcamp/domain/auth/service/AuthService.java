package hello.wink_bootcamp.domain.auth.service;

import hello.wink_bootcamp.domain.auth.dto.LoginRequest;
import hello.wink_bootcamp.domain.auth.dto.LoginResponse;
import hello.wink_bootcamp.domain.auth.exception.AuthException;
import hello.wink_bootcamp.domain.auth.exception.AuthExceptions;
import hello.wink_bootcamp.domain.user.entity.User;
import hello.wink_bootcamp.domain.user.repository.UserRepository;
import hello.wink_bootcamp.global.jwt.TokenProvider;
import hello.wink_bootcamp.global.jwt.redis.TokenBlackListService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final TokenBlackListService tokenBlackListService;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> AuthException.of(AuthExceptions.EMAIL_NOT_REGISTERED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
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
}