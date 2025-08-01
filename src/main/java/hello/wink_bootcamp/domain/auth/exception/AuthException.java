package hello.wink_bootcamp.domain.auth.exception;

import hello.wink_bootcamp.global.exception.ApiException;
import lombok.Getter;

@Getter
public class AuthException extends ApiException {
    private final AuthExceptions errorCode;

    private AuthException(AuthExceptions errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public static AuthException of(AuthExceptions errorCode) {
        return new AuthException(errorCode);
    }

    @Override
    public AuthExceptions getErrorCode() {
        return this.errorCode;
    }
}
