package hello.wink_bootcamp.domain.oauth.dto;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 카카오 OAuth2 사용자 정보 DTO
 * 카카오 API 응답을 파싱하여 필요한 사용자 정보를 추출
 */
@Slf4j
@Getter
public class KakaoUserInfo {
    private final String kakaoId;
    private final String email;
    private final String username;

    public KakaoUserInfo(Map<String, Object> attributes) {
        this.kakaoId = extractKakaoId(attributes);
        this.email = extractEmail(attributes);
        this.username = extractNickname(attributes);

        log.debug("카카오 사용자 정보 파싱 완료 - ID: {}, Email: {}, Nickname: {}",
                kakaoId, email, username);
    }

    private String extractKakaoId(Map<String, Object> attributes) {
        Object id = attributes.get("id");
        return id != null ? String.valueOf(id) : null;
    }

    private String extractEmail(Map<String, Object> attributes) {
        try {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount == null) {
                log.warn("kakao_account 정보가 없습니다.");
                return null;
            }

            String email = (String) kakaoAccount.get("email");
            if (email == null || email.trim().isEmpty()) {
                log.warn("이메일 정보가 없습니다.");
                return null;
            }

            return email;
        } catch (Exception e) {
            log.error("이메일 추출 중 오류 발생", e);
            return null;
        }
    }

    private String extractNickname(Map<String, Object> attributes) {
        try {
            Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
            if (properties == null) {
                log.warn("properties 정보가 없습니다.");
                return null;
            }

            String nickname = (String) properties.get("nickname");
            if (nickname == null || nickname.trim().isEmpty()) {
                log.warn("닉네임 정보가 없습니다.");
                return null;
            }

            return nickname;
        } catch (Exception e) {
            log.error("닉네임 추출 중 오류 발생", e);
            return null;
        }
    }

    /**
     * 필수 정보가 모두 있는지 검증
     */
    public boolean isValid() {
        return kakaoId != null && !kakaoId.trim().isEmpty()
                && email != null && !email.trim().isEmpty()
                && username != null && !username.trim().isEmpty();
    }
}