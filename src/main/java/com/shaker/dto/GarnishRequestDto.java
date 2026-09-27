package com.shaker.dto;

import com.shaker.entity.recipe.GarnishType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GarnishRequestDto(
        @NotBlank @Size(max = 100)
        String description,

        @NotNull
        GarnishType type) {
}
