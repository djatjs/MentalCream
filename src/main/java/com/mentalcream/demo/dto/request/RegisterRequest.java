package com.mentalcream.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,30}$", message = "아이디는 영문, 숫자, 밑줄을 사용해 4~30자로 입력해주세요.")
    private String username;

    @NotBlank
    @Size(min = 8, max = 72, message = "비밀번호는 8~72자로 입력해주세요.")
    private String password;

    @NotBlank
    @Size(min = 1, max = 50)
    private String displayName;
}
