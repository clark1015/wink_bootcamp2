package hello.wink_bootcamp.global.config.jwt;


import hello.wink_bootcamp.domain.user.entity.User; // 이 import는 사용자(User) 엔티티 경로에 맞게 확인해주세요.
import hello.wink_bootcamp.global.config.jwt.exception.AuthException;
import hello.wink_bootcamp.global.config.jwt.exception.AuthExceptions;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
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
    //private Key secretKey; // Key 객체를 저장할 필드 추가



    public String generateToken(User user, Duration expiredAt) {
        Date now = new Date();
        return makeToken(new Date(now.getTime() + expiredAt.toMillis()), user);
    }

    // JWT token 생성 메소드
    private String makeToken(Date expiry, User user) {
        Date now = new Date();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setSubject(user.getEmail())
                .claim("id", user.getId())

                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
                .compact();
    }


    //유효성 검증
    public void validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw AuthException.of(AuthExceptions.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw AuthException.of(AuthExceptions.UNSUPPORTED_TOKEN);
        } catch (MalformedJwtException e) {
            throw AuthException.of(AuthExceptions.MALFORMED_TOKEN);
        } catch (SecurityException | SignatureException e) {
            throw AuthException.of(AuthExceptions.INVALID_TOKEN);
        } catch (IllegalArgumentException e) {
            throw AuthException.of(AuthExceptions.ILLEGAL_TOKEN);
        }
    }
    //토큰 기반 인증 정보 가져오기
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));

        //UserDetails userDetails = new org.springframework.security.core.userdetails.User(claims.getSubject(),"", authorities);

        return new UsernamePasswordAuthenticationToken(new org.springframework.security.core.userdetails.User(claims.getSubject()
                , "", authorities), token, authorities);
    }
    //토큰 기반으로 유저 ID 를 가져오는 메서드
    public Long getUserId(String token) {
        Claims claims = getClaims(token);
        return claims.get("id", Long.class);
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw AuthException.of(AuthExceptions.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw AuthException.of(AuthExceptions.UNSUPPORTED_TOKEN);
        } catch (MalformedJwtException e) {
            throw AuthException.of(AuthExceptions.MALFORMED_TOKEN);
        } catch (SecurityException | SignatureException e) {
            throw AuthException.of(AuthExceptions.INVALID_TOKEN);
        } catch (IllegalArgumentException e) {
            throw AuthException.of(AuthExceptions.ILLEGAL_TOKEN);
        }
    }
}