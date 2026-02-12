package com.mentalcream.demo.dto.request;

import com.mentalcream.demo.domain.Category;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddDoneItemRequest {

    @NotNull
    private Category category;

    @NotNull
    @Size(min = 1, max = 120)
    private String title;

    private Integer minutes;

    private Integer intensity;
}
