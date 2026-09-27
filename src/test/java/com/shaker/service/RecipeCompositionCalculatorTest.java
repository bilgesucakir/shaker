package com.shaker.service;

import com.shaker.entity.recipe.CalorieSource;
import com.shaker.entity.recipe.Glass;
import com.shaker.entity.recipe.IceStyle;
import com.shaker.entity.recipe.Ingredient;
import com.shaker.entity.recipe.IngredientCategory;
import com.shaker.entity.recipe.IngredientLine;
import com.shaker.entity.recipe.IngredientRole;
import com.shaker.entity.recipe.MeasurementUnit;
import com.shaker.entity.recipe.Opacity;
import com.shaker.entity.recipe.PreparationMethod;
import com.shaker.entity.recipe.Recipe;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeCompositionCalculatorTest {

    private final RecipeCompositionCalculator calculator = new RecipeCompositionCalculator();

    private static Ingredient ingredient(String id, String name, IngredientCategory category, String subCategory,
                                         double abvPercent, String colorHex, Set<String> allergenTags, Double caloriesPerOz) {
        Ingredient i = new Ingredient();
        i.setId(id);
        i.setName(name);
        i.setCategory(category);
        i.setSubCategory(subCategory);
        i.setAbvPercent(abvPercent);
        i.setColorHex(colorHex);
        i.setOpacity(Opacity.TRANSLUCENT);
        i.setAllergenTags(allergenTags);
        i.setCaloriesPerOz(caloriesPerOz);
        return i;
    }

    private static IngredientLine line(String ingredientRef, IngredientRole role, double amount, MeasurementUnit unit) {
        IngredientLine line = new IngredientLine();
        line.setIngredientRef(ingredientRef);
        line.setRole(role);
        line.setAmount(amount);
        line.setUnit(unit);
        return line;
    }

    @Test
    void computes_abv_as_volume_weighted_average() {
        // 1oz @ 40% + 1oz @ 16% + 1oz @ 24% over 3oz total = 26.7%
        Ingredient gin = ingredient("gin", "Gin", IngredientCategory.SPIRIT, "gin", 40, "#FFFFFF", Set.of(), 0.0);
        Ingredient vermouth = ingredient("verm", "Sweet Vermouth", IngredientCategory.VERMOUTH, null, 16, "#7B3F00", Set.of(), 0.0);
        Ingredient campari = ingredient("camp", "Campari", IngredientCategory.LIQUEUR, null, 24, "#D7263D", Set.of(), 0.0);
        Map<String, Ingredient> byId = Map.of("gin", gin, "verm", vermouth, "camp", campari);
        List<IngredientLine> lines = List.of(
                line("gin", IngredientRole.BASE_SPIRIT, 1, MeasurementUnit.OZ),
                line("verm", IngredientRole.MODIFIER, 1, MeasurementUnit.OZ),
                line("camp", IngredientRole.MODIFIER, 1, MeasurementUnit.OZ));

        assertThat(calculator.computeAbvEstimate(lines, byId)).isEqualTo(26.7);
    }

    @Test
    void converts_ml_to_ounces_for_abv_math() {
        Ingredient vodka = ingredient("v", "Vodka", IngredientCategory.SPIRIT, "vodka", 40, "#FFF", Set.of(), 0.0);
        Map<String, Ingredient> byId = Map.of("v", vodka);
        // 30ml ~= 1oz, same as if entered directly in oz
        List<IngredientLine> lines = List.of(line("v", IngredientRole.BASE_SPIRIT, 30, MeasurementUnit.ML));

        assertThat(calculator.computeAbvEstimate(lines, byId)).isEqualTo(40.0);
    }

    @Test
    void abv_is_zero_when_no_volumetric_ingredients() {
        List<IngredientLine> lines = List.of(line(null, IngredientRole.BITTERS, 2, MeasurementUnit.DASH));
        assertThat(calculator.computeAbvEstimate(lines, Map.of())).isEqualTo(0.0);
    }

    @Test
    void calorie_estimate_combines_alcohol_and_sugar_and_is_null_when_nothing_resolves() {
        Ingredient rum = ingredient("rum", "Rum", IngredientCategory.SPIRIT, "white_rum", 40, "#FFF", Set.of(), 0.0);
        Map<String, Ingredient> byId = Map.of("rum", rum);
        List<IngredientLine> resolved = List.of(line("rum", IngredientRole.BASE_SPIRIT, 2, MeasurementUnit.OZ));
        List<IngredientLine> unresolved = List.of(line("unknown-ref", IngredientRole.BASE_SPIRIT, 2, MeasurementUnit.OZ));

        assertThat(calculator.computeCalorieEstimate(resolved, byId)).isNotNull().isGreaterThan(0);
        assertThat(calculator.computeCalorieEstimate(unresolved, byId)).isNull();
    }

    @Test
    void base_spirit_tags_only_come_from_base_spirit_lines_that_are_actual_spirits() {
        Ingredient gin = ingredient("gin", "Gin", IngredientCategory.SPIRIT, "gin", 40, "#FFF", Set.of(), 0.0);
        Ingredient vermouth = ingredient("verm", "Sweet Vermouth", IngredientCategory.VERMOUTH, "sweet_vermouth", 16, "#FFF", Set.of(), 0.0);
        Map<String, Ingredient> byId = Map.of("gin", gin, "verm", vermouth);
        List<IngredientLine> lines = List.of(
                line("gin", IngredientRole.BASE_SPIRIT, 1, MeasurementUnit.OZ),
                // a vermouth tagged as MODIFIER, not BASE_SPIRIT - shouldn't show up as a "base spirit"
                line("verm", IngredientRole.MODIFIER, 1, MeasurementUnit.OZ));

        assertThat(calculator.computeBaseSpiritTags(lines, byId)).containsExactly("gin");
    }

    @Test
    void dietary_flags_exclude_claims_when_an_allergen_is_present() {
        Ingredient cream = ingredient("cream", "Cream", IngredientCategory.DAIRY_EGG, null, 0, "#FFF", Set.of("dairy"), 50.0);
        Map<String, Ingredient> byId = Map.of("cream", cream);
        List<IngredientLine> lines = List.of(line("cream", IngredientRole.OTHER, 1, MeasurementUnit.OZ));

        Set<String> flags = calculator.computeDietaryFlags(lines, byId);

        assertThat(flags).contains("egg-free", "gluten-free", "nut-free");
        assertThat(flags).doesNotContain("dairy-free", "vegan");
    }

    @Test
    void dietary_flags_include_vegan_when_nothing_disqualifies_it() {
        Ingredient gin = ingredient("gin", "Gin", IngredientCategory.SPIRIT, "gin", 40, "#FFF", Set.of(), 0.0);
        List<IngredientLine> lines = List.of(line("gin", IngredientRole.BASE_SPIRIT, 1, MeasurementUnit.OZ));

        assertThat(calculator.computeDietaryFlags(lines, Map.of("gin", gin))).contains("vegan");
    }

    @Test
    void layered_drinks_keep_distinct_color_bands_ordered_by_density_densest_first() {
        Ingredient heavy = ingredient("h", "Grenadine", IngredientCategory.SYRUP, null, 0, "#FF0000", Set.of(), 0.0);
        heavy.setRelativeDensity(1.3);
        Ingredient light = ingredient("l", "Soda", IngredientCategory.SODA_MIXER, null, 0, "#00FF00", Set.of(), 0.0);
        light.setRelativeDensity(0.9);
        Map<String, Ingredient> byId = Map.of("h", heavy, "l", light);

        Recipe recipe = new Recipe();
        recipe.setGlass(Glass.HIGHBALL);
        recipe.setIce(IceStyle.ROCKS_CUBED);
        recipe.setMethod(PreparationMethod.LAYERED);
        recipe.setGarnishes(List.of());
        List<IngredientLine> lines = List.of(
                line("l", IngredientRole.MIXER, 4, MeasurementUnit.OZ),
                line("h", IngredientRole.SWEETENER, 1, MeasurementUnit.OZ));

        var spec = calculator.buildImageSpec(recipe, lines, byId);

        assertThat(spec.getColorBands()).hasSize(2);
        assertThat(spec.getColorBands().get(0).getColorHex()).isEqualTo("#FF0000"); // densest first
        assertThat(spec.getColorBands().get(1).getColorHex()).isEqualTo("#00FF00");
    }

    @Test
    void shaken_drinks_blend_into_a_single_color_band() {
        Ingredient a = ingredient("a", "A", IngredientCategory.SPIRIT, null, 40, "#FF0000", Set.of(), 0.0);
        Ingredient b = ingredient("b", "B", IngredientCategory.JUICE, null, 0, "#00FF00", Set.of(), 0.0);
        Map<String, Ingredient> byId = Map.of("a", a, "b", b);

        Recipe recipe = new Recipe();
        recipe.setGlass(Glass.COUPE);
        recipe.setIce(IceStyle.UP);
        recipe.setMethod(PreparationMethod.SHAKEN);
        recipe.setGarnishes(List.of());
        List<IngredientLine> lines = List.of(
                line("a", IngredientRole.BASE_SPIRIT, 1, MeasurementUnit.OZ),
                line("b", IngredientRole.MIXER, 1, MeasurementUnit.OZ));

        var spec = calculator.buildImageSpec(recipe, lines, byId);

        assertThat(spec.getColorBands()).hasSize(1);
        assertThat(spec.getColorBands().get(0).getProportion()).isEqualTo(1.0);
    }

    @Test
    void apply_does_not_overwrite_a_user_entered_calorie_estimate() {
        Recipe recipe = new Recipe();
        recipe.setGlass(Glass.ROCKS);
        recipe.setIce(IceStyle.NEAT);
        recipe.setMethod(PreparationMethod.BUILT);
        recipe.setGarnishes(List.of());
        recipe.setIngredients(List.of());
        recipe.setCalorieEstimate(500);
        recipe.setCalorieSource(CalorieSource.USER_ENTERED);

        calculator.apply(recipe, Map.of());

        assertThat(recipe.getCalorieEstimate()).isEqualTo(500);
        assertThat(recipe.getCalorieSource()).isEqualTo(CalorieSource.USER_ENTERED);
    }
}
