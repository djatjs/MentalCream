package com.mentalcream.demo;

import com.mentalcream.demo.domain.UserAccount;
import com.mentalcream.demo.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:auth-flow;MODE=Oracle;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserAccountRepository userAccountRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void shouldProtectApiAndAllowRegistrationThenLogin() throws Exception {
        mockMvc.perform(get("/api/today").param("date", "2026-08-28"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "Test_User")
                        .param("displayName", "테스터")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        UserAccount saved = userAccountRepository.findByUsername("test_user").orElseThrow();
        assertThat(saved.getPasswordHash()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", saved.getPasswordHash())).isTrue();

        mockMvc.perform(formLogin().user("TEST_USER").password("password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void shouldRenderStatsViewForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/stats-view").with(user("tester")))
                .andExpect(status().isOk())
                .andExpect(view().name("stats"));
    }
}
