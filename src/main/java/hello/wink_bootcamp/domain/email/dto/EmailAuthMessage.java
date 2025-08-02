package hello.wink_bootcamp.domain.email.dto;

import lombok.Builder;

@Builder
public record EmailAuthMessage(
        String email,
        String code
) {}