package com.mentalcream.demo.controller;

import com.mentalcream.demo.domain.UserAccount;
import com.mentalcream.demo.dto.request.RegisterRequest;
import com.mentalcream.demo.security.CurrentUserService;
import com.mentalcream.demo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CurrentUserService currentUserService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid RegisterRequest request, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) return "register";
        try {
            authService.register(request);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            model.addAttribute("registerError", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/api/auth/me")
    @ResponseBody
    public Map<String, String> me(CsrfToken csrfToken) {
        UserAccount user = currentUserService.requireUser();
        return Map.of(
                "username", user.getUsername(),
                "displayName", user.getDisplayName(),
                "csrfToken", csrfToken.getToken(),
                "csrfHeader", csrfToken.getHeaderName()
        );
    }
}
