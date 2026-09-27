package com.shaker.entity.recipe;

import lombok.Getter;
import lombok.Setter;

/**
 * One line of a recipe's ingredient list - a spirit, mixer, herbal element, etc.
 * Embedded in {@link Recipe#getIngredients()}, not its own collection.
 */
@Getter
@Setter
public class IngredientLine {

    /** Link into the {@link Ingredient} catalog, when the ingredient is catalogued. */
    private String ingredientRef;

    /** Fallback name when there's no catalog match (house-made syrup, etc). */
    private String freeTextName;

    private IngredientRole role;

    private Double amount;

    private MeasurementUnit unit;

    /** e.g. "muddled", "expressed peel", "fresh squeezed". */
    private String preparationNote;

    private boolean optional;

    /** Build order - matters for BUILT/LAYERED drinks. */
    private int sequence;
}
