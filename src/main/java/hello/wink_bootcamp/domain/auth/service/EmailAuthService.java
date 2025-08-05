package hello.wink_bootcamp.domain.auth.service;

import hello.wink_bootcamp.domain.auth.dto.response.EmailSendResponse;
import hello.wink_bootcamp.domain.auth.dto.response.EmailVerifyResponse;
import hello.wink_bootcamp.domain.auth.exception.AuthException;
import hello.wink_bootcamp.domain.auth.exception.AuthExceptions;
import hello.wink_bootcamp.domain.email.service.EmailAuthProducer;
import hello.wink_bootcamp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailAuthService {

    private static final String EMAIL_VERIFY_KEY_PREFIX = "email:verify:";
    private static final String VERIFIED_STATUS = "verified";
    private static final Duration CODE_EXPIRY = Duration.ofMinutes(5);
    private static final Duration VERIFICATION_EXPIRY = Duration.ofMinutes(10);
    private static final int CODE_LENGTH = 6;
    private static final int CODE_BOUND = 1_000_000;

    private final RedisTemplate<String, String> redisTemplate;
    private final EmailAuthProducer emailAuthProducer;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 이메일 인증번호 발송
     * @param email 인증번호를 받을 이메일
     */
    public EmailSendResponse sendVerificationCode(String email) {
        validateEmailNotRegistered(email);

        String verificationCode = generateSecureRandomCode();
        storeVerificationCode(email, verificationCode);
        sendEmailAsync(email, verificationCode);

        log.info("인증번호 발송 완료: {}", email);
        return new EmailSendResponse("인증번호가 발송되었습니다.");
    }

    /**
     * 이메일 인증번호 검증
     * @param email 검증할 이메일
     * @param inputCode 사용자 입력 인증번호
     * @return 인증 결과 응답
     */
    public EmailVerifyResponse verifyCode(String email, String inputCode) {
        String verificationKey = buildVerificationKey(email);
        String savedCode = getSavedVerificationCode(verificationKey);

        validateVerificationCode(savedCode, inputCode);
        markEmailAsVerified(verificationKey);

        log.info("이메일 인증 성공: {}", email);
        return new EmailVerifyResponse("인증이 성공했습니다.");
    }


    private void validateEmailNotRegistered(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("이미 가입된 이메일로 인증 시도: {}", email);
            throw AuthException.of(AuthExceptions.EMAIL_ALREADY_EXISTS);
        }
    }

    private String generateSecureRandomCode() {
        return String.format("%0" + CODE_LENGTH + "d", secureRandom.nextInt(CODE_BOUND));
    }

    private void storeVerificationCode(String email, String code) {
        String key = buildVerificationKey(email);
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRY);
    }

    private void sendEmailAsync(String email, String code) {
        try {
            emailAuthProducer.sendEmail(email, code);
        } catch (Exception e) {
            log.error("이메일 발송 실패: {} - {}", email, e.getMessage());
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }

    private String buildVerificationKey(String email) {
        return EMAIL_VERIFY_KEY_PREFIX + email;
    }

    private String getSavedVerificationCode(String key) {
        String savedCode = redisTemplate.opsForValue().get(key);
        if (savedCode == null || VERIFIED_STATUS.equals(savedCode)) {
            throw AuthException.of(AuthExceptions.CODE_NOT_FOUND);
        }
        return savedCode;
    }

    private void validateVerificationCode(String savedCode, String inputCode) {
        if (!savedCode.equals(inputCode)) {
            throw AuthException.of(AuthExceptions.CODE_MISMATCH);
        }
    }

    private void markEmailAsVerified(String key) {
        redisTemplate.delete(key);
        redisTemplate.opsForValue().set(key, VERIFIED_STATUS, VERIFICATION_EXPIRY);
    }
}