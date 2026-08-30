package com.mentalcream.demo.security;

import com.mentalcream.demo.domain.UserAccount;
import com.mentalcream.demo.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserAccountRepository userAccountRepository;

    public UserAccount requireUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return userAccountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("인증된 사용자를 찾을 수 없습니다."));
    }
}
