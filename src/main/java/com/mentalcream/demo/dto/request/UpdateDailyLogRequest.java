package com.mentalcream.demo.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDailyLogRequest {
    @Min(1)
    @Max(5)
    private Integer mood;

    @Min(1)
    @Max(5)
    private Integer energy;

    private String worryText;

    @Min(0)
    @Max(5)
    private Integer worryIntensity;

    @Size(max = 500)
    private String note;
}
