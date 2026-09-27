package com.shaker.dto;

import jakarta.validation.constraints.NotBlank;

public record BookmarkRequestDto(@NotBlank String recipeId) {
}
