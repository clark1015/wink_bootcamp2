package hello.wink_bootcamp.global.config.jwt;


import hello.wink_bootcamp.domain.user.entity.User; // 이 import는 사용자(User) 엔티티 경로에 맞게 확인해주세요.
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class TokenProvider {

    private final JwtProperties jwtProperties;
    private Key secretKey;

    // 의존성 주입 후 초기화를 수행하는 메소드. 한 번만 실행됨.
    @PostConstruct
    private void init() {
        byte[] keyBytes = jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 지정된 유저 정보와 만료 기간을 바탕으로 JWT 토큰을 생성합니다.
     * @param user 유저 정보
     * @param expiredAt 만료 시간
     * @return 생성된 JWT 토큰
     */
    public String generateToken(User user, Duration expiredAt) {
        Date now = new Date();
        return makeToken(new Date(now.getTime() + expiredAt.toMillis()), user);
    }

    /**
     * JWT 토큰을 생성하는 핵심 로직
     */
    private String makeToken(Date expiry, User user) {
        Date now = new Date();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE) // 헤더 typ: JWT
                .setIssuer(jwtProperties.getIssuer())         // 발급자
                .setIssuedAt(now)                             // 발급일시
                .setExpiration(expiry)                        // 만료일시
                .setSubject(user.getEmail())                  // 토큰 제목 (주로 이메일)
                .claim("id", user.getUserid())              // 클레임: 유저 ID
                .signWith(secretKey, SignatureAlgorithm.HS256) // HS256 알고리즘과 비밀 키로 서명
                .compact();                                   // 토큰 생성
    }

    /**
     * 주어진 JWT 토큰의 유효성을 검증합니다.
     * @param token 검증할 JWT 토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean validToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey) // 비밀 키로 서명 검증
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // 토큰이 유효하지 않을 경우 (만료, 서명 불일치 등)
            return false;
        }
    }

    /**
     * JWT 토큰을 기반으로 Spring Security의 Authentication 객체를 반환합니다.
     * @param token JWT 토큰
     * @return 인증 정보
     */
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));

        org.springframework.security.core.userdetails.User user =
                new org.springframework.security.core.userdetails.User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(user, token, authorities);
    }

    /**
     * 토큰에서 사용자 ID를 추출합니다.
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public Long getUserId(String token) {
        Claims claims = getClaims(token);
        return claims.get("id", Long.class);
    }

    /**
     * 토큰에서 Claims(정보)를 추출하는 내부 메소드
     */
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}