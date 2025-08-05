package hello.wink_bootcamp.domain.oauth.service;

import hello.wink_bootcamp.domain.oauth.dto.KakaoUserInfo;
import hello.wink_bootcamp.domain.user.entity.User;
import hello.wink_bootcamp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    // 상수 정의
    private static final String KAKAO_REGISTRATION_ID = "kakao";
    private static final String DEFAULT_ROLE = "ROLE_USER";
    private static final String OAUTH2_NAME_ATTRIBUTE = "id";

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oauth2User);
        } catch (OAuth2AuthenticationException ex) {
            // OAuth2 관련 예외는 그대로 전파
            throw ex;
        } catch (Exception ex) {
            log.error("OAuth2 사용자 처리 중 예상치 못한 오류 발생", ex);
            throw new OAuth2AuthenticationException("로그인 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        if (!KAKAO_REGISTRATION_ID.equals(registrationId)) {
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        }

        KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(oauth2User.getAttributes());

        // 필수 정보 검증
        if (!kakaoUserInfo.isValid()) {
            throw new OAuth2AuthenticationException("카카오로부터 필수 정보(이메일, 닉네임)를 가져올 수 없습니다.");
        }

        User user = saveOrUpdate(kakaoUserInfo);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(DEFAULT_ROLE)),
                oauth2User.getAttributes(),
                OAUTH2_NAME_ATTRIBUTE
        );
    }

    private User saveOrUpdate(KakaoUserInfo kakaoUserInfo) {
        User user = userRepository.findByKakaoId(kakaoUserInfo.getKakaoId())
                .map(existingUser -> updateExistingUser(existingUser, kakaoUserInfo))
                .orElseGet(() -> createNewUser(kakaoUserInfo));

        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, KakaoUserInfo kakaoUserInfo) {
        log.info("기존 사용자 로그인: {}", existingUser.getEmail());
        existingUser.updateKakaoInfo(kakaoUserInfo.getUsername());
        return existingUser;
    }

    private User createNewUser(KakaoUserInfo kakaoUserInfo) {
        String email = kakaoUserInfo.getEmail();
        String username = kakaoUserInfo.getUsername();
        String kakaoId = kakaoUserInfo.getKakaoId();

        // 이메일 중복 체크
        if (userRepository.existsByEmail(email)) {
            throw new OAuth2AuthenticationException("이미 가입된 이메일입니다: " + email);
        }

        // 고유한 사용자명 생성
        username = generateUniqueUsername(username);

        log.info("새 사용자 생성: {}", email);

        return User.createKakaoUser(email, username, kakaoId);
    }

    /**
     * 동시성 문제를 해결한 고유 사용자명 생성
     * 타임스탬프를 활용하여 중복 가능성을 최소화
     */
    private String generateUniqueUsername(String baseUsername) {
        // 현재 시간을 밀리초 단위로 포맷팅 (yyyyMMddHHmmssSSS)
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String candidateUsername = baseUsername + "_" + timestamp;

        // 극히 드문 경우를 대비한 추가 확인 및 대체 로직
        if (userRepository.existsByUsername(candidateUsername)) {
            // 나노초 추가로 거의 100% 고유성 보장
            long nanoTime = System.nanoTime() % 1000000; // 마지막 6자리만 사용
            candidateUsername = baseUsername + "_" + timestamp + "_" + nanoTime;
        }

        return candidateUsername;
    }
}