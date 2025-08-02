package hello.wink_bootcamp.global.config.jwt;

import hello.wink_bootcamp.domain.user.entity.User;
import hello.wink_bootcamp.domain.user.repository.UserRepository;
import hello.wink_bootcamp.global.jwt.JwtProperties;
import hello.wink_bootcamp.global.jwt.TokenProvider;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TokenproviderTest {
}



@SpringBootTest
class TokenProviderTest {
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtProperties jwtProperties;

    @Test
    @DisplayName("generateToken(): 유저 정보와 만료 기간을 전달해 토큰을 만들 수 있다.")
    void generateToken() {
        User testUser = userRepository.save(User.builder()
                .email("user@gmail.com")
                .password("test")
                .build());

        String token = tokenProvider.generateToken(testUser, Duration.ofDays(14));

        Long userId = Jwts.parser() // 1. parser() 대신 parserBuilder() 사용
                .setSigningKey(jwtProperties.getSecretKey()) // 2. 서명 키 설정
                .parseClaimsJws(token) // 4. 토큰 파싱 (이제 경고가 사라짐)
                .getBody()
                .get("id", Long.class);

        assertThat(userId).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("validToken(): 만료된 토큰인 때에 유효성 거증에 실패한다.")
    void validToken_invalidToken() {
        String token = JwtFactory.builder()
                .expiration(new Date(new Date().getTime() - Duration.ofDays(7).
                        toMillis()))
                .build()
                .createToken(jwtProperties);
        boolean result = tokenProvider.validToken(token);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("validToken(): 유효한 토큰인 때에 유효성 검증에 성공한다.")
    void validToken_validToken() {
        String token = JwtFactory.withDefaultValues()
                .createToken(jwtProperties);

        boolean result = tokenProvider.validToken(token);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("getAuthentication(): 토큰 기반으로 인증 정보를 가져올 수 있다.")
    void getAuthentication() {
        String userEmail = "user@email.com";
        String token = JwtFactory.builder()
                .subject(userEmail)
                .build()
                .createToken(jwtProperties);

        Authentication authentication = tokenProvider.getAuthentication(token);

        assertThat(((UserDetails) authentication.getPrincipal()).getUsername()).
                isEqualTo(userEmail);
    }

    @Test
    @DisplayName("getUserId(): 토큰으로 유저 ID를 가져올 수 있다.")
    void getUserId() {
        Long userId = 1L;
        String token = JwtFactory.builder()
                .claims(Map.of("id", userId))
                .build()
                .createToken(jwtProperties);

        Long userIdByToken = tokenProvider.getUserId(token);

        assertThat(userIdByToken).isEqualTo(userId);
    }
}
