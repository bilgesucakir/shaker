package com.shaker.dto;

import com.shaker.entity.common.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecipeCollectionRequestDto(
        @NotBlank @Size(max = 80)
        String name,

        @Size(max = 500)
        String description,

        @NotNull
        Visibility visibility) {
}
