package hello.wink_bootcamp.domain.user.service;

import hello.wink_bootcamp.domain.auth.CustomUserDetails;
import hello.wink_bootcamp.domain.user.dto.MyPageResponse;
import hello.wink_bootcamp.domain.user.entity.User;
import hello.wink_bootcamp.domain.user.exception.UserException;
import hello.wink_bootcamp.domain.user.exception.UserExceptions;
import hello.wink_bootcamp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public MyPageResponse getMyPage(CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserException.of(UserExceptions.USER_NOT_FOUND));
        return MyPageResponse.of(user);
    }
}
