package com.shaker.dto;

import com.shaker.entity.recipe.Difficulty;
import com.shaker.entity.recipe.Glass;
import com.shaker.entity.recipe.IceStyle;
import com.shaker.entity.recipe.PreparationMethod;
import com.shaker.entity.recipe.RecipeCategory;
import com.shaker.entity.recipe.TasteNote;
import com.shaker.entity.common.Visibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

/**
 * Shared payload for creating a recipe, saving a new version of one, forking one, or editing
 * one in place - the target operation is decided by which endpoint it's sent to.
 */
public record RecipeRequestDto(
        @NotBlank @Size(max = 120)
        String name,

        RecipeCategory category,

        Set<String> baseSpiritTags,

        @NotNull
        Glass glass,

        @NotNull
        IceStyle ice,

        @NotNull
        PreparationMethod method,

        @NotEmpty @Valid
        List<IngredientLineRequestDto> ingredients,

        @Valid
        List<GarnishRequestDto> garnishes,

        List<@NotBlank String> instructions,

        @Min(1) @Max(20)
        int servings,

        Set<TasteNote> tasteProfile,

        Difficulty difficulty,

        Set<String> tags,

        @Size(max = 2000)
        String description,

        List<String> photos,

        @NotNull
        Visibility visibility,

        /** Manual calorie override; leave null to have it calculated from ingredients. */
        Integer calorieEstimate) {
}
