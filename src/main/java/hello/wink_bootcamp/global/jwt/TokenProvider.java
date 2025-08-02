package hello.wink_bootcamp.global.jwt;


import hello.wink_bootcamp.domain.auth.CustomUserDetails;
import hello.wink_bootcamp.domain.user.entity.User; // 이 import는 사용자(User) 엔티티 경로에 맞게 확인해주세요.
import hello.wink_bootcamp.domain.auth.exception.AuthException;
import hello.wink_bootcamp.domain.auth.exception.AuthExceptions;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@RequiredArgsConstructor
@Service
@Slf4j
public class TokenProvider {
    private final JwtProperties jwtProperties;
    private Key secretKey;

    @PostConstruct
    protected void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecretKey());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user, Duration expiredAt) {
        Date now = new Date();
        return makeToken(new Date(now.getTime() + expiredAt.toMillis()), user);
    }

    // JWT token 생성 메소드
    private String makeToken(Date expiry, User user) {
        Date now = new Date();

        log.debug("🔐 secretKey = {}", secretKey);
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setSubject(user.getEmail())
                .claim("id", user.getUserid())
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }


    //유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
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

        Long userId = claims.get("id", Long.class);
        String email = claims.getSubject(); // subject에 email 넣었다고 가정

        Collection<GrantedAuthority> authorities =
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));

        CustomUserDetails userDetails = new CustomUserDetails(userId, email, authorities);

        return new UsernamePasswordAuthenticationToken(userDetails, token, authorities);
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

    public long getRemainingExpiration(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();

        long now = System.currentTimeMillis();
        return Math.max((expiration.getTime() - now) / 1000, 0) ; // 초 단위
    }
}