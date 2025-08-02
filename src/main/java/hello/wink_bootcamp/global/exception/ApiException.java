package hello.wink_bootcamp.global.exception;

public abstract class ApiException extends RuntimeException {
    public ApiException(String message) {
      super(message);
    }
    public abstract BaseErrorCode getErrorCode();
}
