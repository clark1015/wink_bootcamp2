package hello.wink_bootcamp.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private final int status;   //401
    private final String message; //사용자를 찾을 수 없습니다
}