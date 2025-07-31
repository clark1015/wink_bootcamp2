package hello.wink_bootcamp.global.exception;

import hello.wink_bootcamp.global.config.jwt.exception.AuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                        e.getErrorCode().getCode(),     // AUTH_001
                        e.getErrorCode().getMessage()   // 만료된 토큰입니다.
                ));
    }
}
