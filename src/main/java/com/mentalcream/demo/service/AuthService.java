package com.mentalcream.demo.service;

import com.mentalcream.demo.domain.UserAccount;
import com.mentalcream.demo.dto.request.RegisterRequest;
import com.mentalcream.demo.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterRequest request) {
        String username = request.getUsername().trim().toLowerCase(Locale.ROOT);
        if (userAccountRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        userAccountRepository.save(UserAccount.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName().trim())
                .build());
    }
}
