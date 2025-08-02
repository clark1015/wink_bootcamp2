package hello.wink_bootcamp.domain.user.exception;

import hello.wink_bootcamp.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserExceptions implements BaseErrorCode {

    //조회 관련
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    INVALID_USER_ID(400, "유효하지 않은 사용자 ID입니다."),
    INACTIVE_USER(403, "비활성화된 사용자입니다."),

    //회원가입 관련
    DUPLICATE_EMAIL(409, "이미 존재하는 이메일입니다."),
    DUPLICATE_NICKNAME(409, "이미 존재하는 닉네임입니다."),
    INVALID_EMAIL_FORMAT(400, "올바르지 않은 이메일 형식입니다."),
    INVALID_PASSWORD_FORMAT(400, "비밀번호는 8자 이상이어야 하며, 특수문자를 포함해야 합니다."),
    INVALID_NICKNAME_FORMAT(400, "닉네임은 2~20자의 영문 또는 숫자여야 합니다."),

    //비밀번호 관련
    INVALID_PASSWORD(401, "잘못된 비밀번호입니다."),
    SAME_AS_OLD_PASSWORD(400, "기존 비밀번호와 동일합니다."),
    PASSWORD_CONFIRMATION_MISMATCH(400, "비밀번호 확인이 일치하지 않습니다."),

    //이메일 인증 관련
    EMAIL_NOT_VERIFIED(401, "이메일 인증이 완료되지 않았습니다."),
    EMAIL_VERIFICATION_FAILED(401, "이메일 인증 코드가 올바르지 않습니다."),

    //시스템 관련
    USER_INTERNAL_ERROR(500, "사용자 처리 중 알 수 없는 오류가 발생했습니다.");

    private final int status;
    private final String message;

}
