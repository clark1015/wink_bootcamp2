package hello.wink_bootcamp.domain.auth.exception;

import hello.wink_bootcamp.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthExceptions implements BaseErrorCode {

    //로그인 관련
    INVALID_PASSWORD(401, "비밀번호가 올바르지 않습니다."),
    EMAIL_NOT_REGISTERED(404, "가입되지 않은 이메일입니다."),
    INACTIVE_ACCOUNT(403, "비활성화된 계정입니다."),

    //토큰 관련
    MISSING_TOKEN(401, "Access Token이 존재하지 않습니다."),
    EXPIRED_TOKEN(401, "Access Token이 만료되었습니다."),
    INVALID_TOKEN(401, "유효하지 않은 Access Token입니다."),
    MALFORMED_TOKEN(401, "손상된 JWT입니다."),
    UNSUPPORTED_TOKEN(401, "지원되지 않는 JWT 형식입니다."),
    ILLEGAL_TOKEN(401, "JWT가 올바르지 않은 구조입니다."),
    BLACKLISTED_TOKEN(401, "이미 로그아웃된 토큰입니다."),

    //로그아웃 관련
    ALREADY_LOGGED_OUT(400, "이미 로그아웃된 사용자입니다."),
    UNAUTHORIZED_LOGOUT(401, "로그인한 사용자만 로그아웃할 수 있습니다."),

    //시스템 관련
    INTERNAL_ERROR(500, "인증 처리 중 알 수 없는 에러가 발생했습니다.");

    private final int status;
    private final String message;
}