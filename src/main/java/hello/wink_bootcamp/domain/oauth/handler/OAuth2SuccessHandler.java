package hello.wink_bootcamp.domain.oauth.handler;

import hello.wink_bootcamp.domain.user.entity.User;
import hello.wink_bootcamp.domain.user.repository.UserRepository;
import hello.wink_bootcamp.global.jwt.JwtProperties;
import hello.wink_bootcamp.global.jwt.TokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional(readOnly = true)
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        // 카카오 ID 추출
        String kakaoId = extractKakaoId(oauth2User);
        if (kakaoId == null) {
            log.error("카카오 ID를 찾을 수 없습니다.");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "카카오 ID를 찾을 수 없습니다.");
            return;
        }

        // 사용자 조회
        Optional<User> userOptional = userRepository.findByKakaoId(kakaoId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // JWT 토큰 생성
            String accessToken = tokenProvider.generateAccessToken(user, Duration.ofMinutes(jwtProperties.getAccessTokenExpiration()));
            String refreshToken = tokenProvider.generateRefreshToken(user, Duration.ofDays(jwtProperties.getRefreshTokenExpiration()));

            log.info("카카오 로그인 성공 - 사용자: {}", user.getEmail());

            // 리프레시 토큰을 쿠키로 설정
            setRefreshTokenCookie(response, refreshToken);

            // 현재: 백엔드 테스트용 URL 파라미터 방식
            redirectWithTokens(accessToken, refreshToken, request, response);

            // TODO: 프론트엔드 연결 시 아래 메서드로 변경
            // redirectSecure(accessToken, refreshToken, request, response);

        } else {
            log.error("사용자를 찾을 수 없음: kakaoId={}", kakaoId);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "사용자 정보를 찾을 수 없습니다.");
        }
    }

    private String extractKakaoId(OAuth2User oauth2User) {
        Object id = oauth2User.getAttribute("id");
        return id != null ? String.valueOf(id) : null;
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);   // JavaScript 접근 불가 (보안)
        refreshTokenCookie.setSecure(false);    // 개발환경: false, 프로덕션: true
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) Duration.ofDays(jwtProperties.getRefreshTokenExpiration()).getSeconds());
        response.addCookie(refreshTokenCookie);
    }

    // =================================================================
    // 현재 사용 중: 백엔드 테스트용 (URL 파라미터 방식)
    // =================================================================
    private void redirectWithTokens(String accessToken,
                                    String refreshToken,
                                    HttpServletRequest request,
                                    HttpServletResponse response) throws IOException {

        String redirectUri = "http://suntcamp-auth.junhwan.me/oauth2/redirect"
                + "?accessToken=" + accessToken
                + "&refreshToken=" + refreshToken
                + "&message=login_success";

        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }

    // =================================================================
    // 추후 사용 예정: 프론트엔드 연결용 (보안 강화 쿠키 방식)
    // =================================================================

    /**
     * 보안 강화된 토큰 처리 - 프론트엔드 연결 시 사용
     * AccessToken: 쿠키 (JavaScript 접근 가능)
     * RefreshToken: HttpOnly 쿠키 (JavaScript 접근 불가)
     */
    private void redirectSecure(String accessToken,
                                String refreshToken,
                                HttpServletRequest request,
                                HttpServletResponse response) throws IOException {

        // AccessToken을 JavaScript 접근 가능한 쿠키로 설정
        setAccessTokenCookie(response, accessToken);

        // RefreshToken은 이미 HttpOnly 쿠키로 설정됨

        // URL에는 토큰 없이 리다이렉트 (보안 강화)
        String redirectUri = "http://localhost:3000/auth/success?message=login_success";

        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }

    /**
     * AccessToken 쿠키 설정 (JavaScript 접근 가능)
     */
    private void setAccessTokenCookie(HttpServletResponse response, String accessToken) {
        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(false);  // JavaScript에서 접근 가능
        accessTokenCookie.setSecure(false);    // 개발환경: false, 프로덕션: true
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(900);      // 15분 (AccessToken 수명과 동일)

        response.addCookie(accessTokenCookie);
    }

    /**
     * JSON 응답 방식 - SPA에서 AJAX 로그인 시 사용
     */
    private void sendJsonResponse(HttpServletResponse response,
                                  String accessToken,
                                  String refreshToken,
                                  User user) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // RefreshToken은 HttpOnly 쿠키로만 전송 (보안)
        setRefreshTokenCookie(response, refreshToken);

        // AccessToken과 사용자 정보만 JSON으로 응답
        String jsonResponse = String.format(
                "{\"accessToken\":\"%s\",\"tokenType\":\"Bearer\",\"user\":{\"id\":%d,\"email\":\"%s\",\"username\":\"%s\"}}",
                accessToken, user.getUserid(), user.getEmail(), user.getUsername()
        );

        response.getWriter().write(jsonResponse);
    }
}