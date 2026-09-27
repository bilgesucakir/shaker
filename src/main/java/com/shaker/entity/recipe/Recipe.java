package com.shaker.entity.recipe;

import com.shaker.entity.common.BaseEntity;
import com.shaker.entity.common.Visibility;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A cocktail definition - reusable and shareable, as opposed to a diary entry which is a
 * one-off logged occasion of drinking something. Covers app-seeded classics, a user's own
 * originals, edited versions of their own recipes, and forks of someone else's/a classic's.
 *
 * <p>Extends {@link BaseEntity} rather than a "user-owned" base: {@link #getCreatedBy()} is
 * nullable for app classics, so it doesn't fit an "always has one owner" contract.
 */
@Getter
@Setter
@Document("recipes")
public class Recipe extends BaseEntity {

    private String name;

    /**
     * Groups the versions of one recipe together (see {@link #getVersion()}). Null until a
     * recipe is actually versioned - a fresh, never-edited recipe doesn't need one yet.
     */
    @Indexed
    private String recipeFamilyId;

    /** Attribution when this recipe started life as someone else's/a classic's, but is its own lineage. */
    private String forkedFromRecipeId;

    private RecipeOrigin origin;

    /** Null for app classics. */
    @Indexed
    private String createdBy;

    private Visibility visibility = Visibility.PRIVATE;

    private RecipeCategory category;

    /** Denormalized from ingredients, for filtering ("only gin-based drinks"). */
    private Set<String> baseSpiritTags = new LinkedHashSet<>();

    private Glass glass;

    private IceStyle ice;

    private PreparationMethod method;

    private List<IngredientLine> ingredients = new ArrayList<>();

    private List<Garnish> garnishes = new ArrayList<>();

    private List<String> instructions = new ArrayList<>();

    private int servings = 1;

    /** Computed from ingredient volumes x ABV, or set manually. */
    private Double abvEstimate;

    private Set<TasteNote> tasteProfile = new LinkedHashSet<>();

    /** Rolled up from ingredient allergen tags, e.g. "vegan", "dairy-free", "gluten-free". */
    private Set<String> dietaryFlags = new LinkedHashSet<>();

    private Difficulty difficulty;

    private Set<String> tags = new LinkedHashSet<>();

    private String description;

    /** Reference photo(s) of this recipe/version. */
    private List<String> photos = new ArrayList<>();

    /** Drives the drawn (non-photo) glass illustration; null until generated. */
    private GeneratedImageSpec generatedImageSpec;

    /** Either calculated from ingredients or entered manually - see {@link #getCalorieSource()}. */
    private Integer calorieEstimate;

    private CalorieSource calorieSource;

    private double ratingAverage;

    private int ratingCount;

    /** 1 for a first/only version; bumped when saved as a new version within the same family. */
    private int version = 1;

    private Instant updatedAt = Instant.now();

    public boolean isMocktail() {
        return abvEstimate != null && abvEstimate == 0;
    }
}
