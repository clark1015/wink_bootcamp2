package hello.wink_bootcamp.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;


public record LoginResponse(Long userId, String email, String accessToken)
{}
