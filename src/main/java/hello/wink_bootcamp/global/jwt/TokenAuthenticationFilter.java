package hello.wink_bootcamp.global.jwt;

import hello.wink_bootcamp.domain.auth.exception.AuthException;
import hello.wink_bootcamp.domain.auth.exception.AuthExceptions;
import hello.wink_bootcamp.global.jwt.redis.TokenBlackListService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;
    private final TokenBlackListService blacklistService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);
            String token = extractToken(authorizationHeader);

            if (token != null && tokenProvider.validateToken(token)) {
                if (blacklistService.isBlacklisted(token)) {
                    throw AuthException.of(AuthExceptions.EXPIRED_TOKEN); // 로그아웃된 토큰
                }

                Authentication authentication = tokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("인증 정보 설정 완료: {}", authentication.getName());
            }

        } catch (AuthException e) {
            log.warn("JWT 인증 실패: {}", e.getErrorCode().getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":\"" + e.getErrorCode().name() + "\", \"message\":\"" + e.getErrorCode().getMessage() + "\"}");
            return; // 인증 실패 시 다음 필터로 넘기지 않음
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(String header) {
        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            return header.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}