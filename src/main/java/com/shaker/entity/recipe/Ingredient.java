package com.shaker.entity.recipe;

import com.shaker.entity.common.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Reference catalog of spirits, mixers, syrups, etc. Normalizing these (rather than free
 * text on every recipe) is what makes ABV/calorie estimates and the generated illustration's
 * colors possible, and lets "what can I make with what I own" matching work.
 */
@Getter
@Setter
@Document("ingredients")
public class Ingredient extends BaseEntity {

    @Indexed(unique = true)
    private String name;

    private Set<String> aliases = new LinkedHashSet<>();

    private IngredientCategory category;

    /** e.g. "gin", "rye_whiskey", "mezcal" under SPIRIT. */
    private String subCategory;

    private double abvPercent;

    /** Representative color, used to render the generated glass illustration. */
    private String colorHex;

    private Opacity opacity;

    /** Relative density - which layer floats/sinks in a LAYERED drink. Higher sinks. */
    private Double relativeDensity;

    /** What this ingredient contains, e.g. "dairy", "egg", "gluten", "nuts" - used to roll
     *  up safe claims ("dairy-free", "vegan"...) onto a Recipe. Empty means none of these. */
    private Set<String> allergenTags = new LinkedHashSet<>();

    /** Used by the calorie calculator; null if unknown. */
    private Double caloriesPerOz;
}
