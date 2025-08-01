package hello.wink_bootcamp.domain.user.exception;

import hello.wink_bootcamp.global.exception.ApiException;
import lombok.Getter;

@Getter
public class UserException extends ApiException {
    private final UserExceptions errorCode;

    private UserException(UserExceptions errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public static UserException of(UserExceptions errorCode) {
        return new UserException(errorCode);
    }

    @Override
    public UserExceptions getErrorCode() {
        return this.errorCode;
    }

}