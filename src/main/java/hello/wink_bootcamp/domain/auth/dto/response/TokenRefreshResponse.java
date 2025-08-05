package hello.wink_bootcamp.domain.auth.dto.response;

public record TokenRefreshResponse(
        String accessToken,
        String refreshToken
) {
    public String tokenType() {
        return "Bearer";
    }
}
