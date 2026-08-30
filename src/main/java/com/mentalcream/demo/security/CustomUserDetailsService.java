package com.mentalcream.demo.security;

import com.mentalcream.demo.domain.UserAccount;
import com.mentalcream.demo.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedUsername = username.trim().toLowerCase(Locale.ROOT);
        UserAccount account = userAccountRepository.findByUsername(normalizedUsername)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        return new User(account.getUsername(), account.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
