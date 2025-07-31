package hello.wink_bootcamp.global.config.jwt.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthExceptions {

    EXPIRED_TOKEN("AUTH_001", "만료된 토큰입니다."),
    INVALID_TOKEN("AUTH_002", "유효하지 않은 토큰입니다."),
    MALFORMED_TOKEN("AUTH_003", "비정상적인 토큰 형식입니다."),
    UNSUPPORTED_TOKEN("AUTH_004", "지원하지 않는 토큰 형식입니다."),
    ILLEGAL_TOKEN("AUTH_005", "토큰 파싱 중 예외가 발생했습니다.");

    private final String code;     // 예: AUTH_001
    private final String message;  // 예: 만료된 토큰입니다
}