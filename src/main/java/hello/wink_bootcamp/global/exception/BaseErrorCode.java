package hello.wink_bootcamp.global.exception;

public interface BaseErrorCode {
    int getStatus();
    String getMessage();
    String name(); //enum 이름 그대로
}
