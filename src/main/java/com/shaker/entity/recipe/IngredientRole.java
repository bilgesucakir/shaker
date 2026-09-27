package com.shaker.entity.recipe;

/** What an ingredient line is doing in a recipe - drives grouping/search ("what's the mixer?"). */
public enum IngredientRole {
    BASE_SPIRIT,
    MODIFIER,
    MIXER,
    SWEETENER,
    BITTERS,
    HERBAL_BOTANICAL,
    DAIRY_EGG,
    OTHER
}
