package hello.wink_bootcamp.domain.auth.dto.request;

public record TokenRefreshRequest(
        String refreshToken,
        String accessToken
) {}