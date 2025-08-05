package hello.wink_bootcamp.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
public record LoginResponse(
        Long userId,
        String email,
        String accessToken,
        String refreshToken
)
{}
