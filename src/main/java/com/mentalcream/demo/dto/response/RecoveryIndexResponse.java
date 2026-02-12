package com.mentalcream.demo.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecoveryIndexResponse {
    private int score;
    private String status;
}
