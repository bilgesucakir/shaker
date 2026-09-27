package com.shaker.dto;

import com.shaker.entity.recipe.IngredientRole;
import com.shaker.entity.recipe.MeasurementUnit;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record IngredientLineRequestDto(
        String ingredientRef,

        @Size(max = 80)
        String freeTextName,

        @NotNull
        IngredientRole role,

        @NotNull @Positive
        Double amount,

        @NotNull
        MeasurementUnit unit,

        @Size(max = 200)
        String preparationNote,

        boolean optional,

        int sequence) {
}
