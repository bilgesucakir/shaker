package com.shaker.dto;

import java.util.List;

/**
 * "You have all ingredients!" - result of checking a recipe's non-optional ingredients
 * against a user's bar inventory.
 */
public record RecipeAvailabilityDto(
        boolean hasAllIngredients,
        List<String> missingIngredientNames) {
}
