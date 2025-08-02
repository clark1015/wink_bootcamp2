package hello.wink_bootcamp.domain.auth.dto.response;

public record RegisterResponse(
        Long userId,
        String email,
        String username,
        String message
) {}