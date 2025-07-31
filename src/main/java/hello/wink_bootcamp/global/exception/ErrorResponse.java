package hello.wink_bootcamp.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private final String code;   // 예: USER_NOT_FOUND
    private final String message; // 예: 사용자를 찾을 수 없습니다
}