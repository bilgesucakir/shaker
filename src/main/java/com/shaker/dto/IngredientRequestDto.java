package com.shaker.dto;

import com.shaker.entity.recipe.IngredientCategory;
import com.shaker.entity.recipe.Opacity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record IngredientRequestDto(
        @NotBlank @Size(max = 80)
        String name,

        Set<String> aliases,

        @NotNull
        IngredientCategory category,

        @Size(max = 40)
        String subCategory,

        @NotNull @PositiveOrZero
        Double abvPercent,

        @NotBlank
        String colorHex,

        @NotNull
        Opacity opacity,

        Double relativeDensity,

        Set<String> allergenTags,

        Double caloriesPerOz) {
}
