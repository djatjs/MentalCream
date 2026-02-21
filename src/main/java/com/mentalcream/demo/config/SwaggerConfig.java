package com.mentalcream.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mental Cream REST API")
                        .description("불안한 취준생의 통제감 회복 시스템 API 명세서")
                        .version("v1.0.0"));
    }
}
