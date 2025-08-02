package hello.wink_bootcamp.global.exception.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.wink_bootcamp.domain.auth.exception.AuthExceptions;
import hello.wink_bootcamp.global.exception.ApiException;
import hello.wink_bootcamp.global.exception.BaseErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (ApiException e) {
            log.warn(" ApiException 발생: {}", e.getErrorCode().getMessage());
            sendErrorResponse(response, e.getErrorCode());
        } catch (Exception e) {
            log.error(" 처리되지 않은 서버 오류 발생", e);
            sendErrorResponse(response, AuthExceptions.INTERNAL_ERROR);  // fallback
        }
    }

    private void sendErrorResponse(HttpServletResponse response, BaseErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus());
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("code", errorCode.name());
        errorBody.put("message", errorCode.getMessage());

        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }
}