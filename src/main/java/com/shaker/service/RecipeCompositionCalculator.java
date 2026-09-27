package com.shaker.service;

import com.shaker.entity.recipe.CalorieSource;
import com.shaker.entity.recipe.ColorBand;
import com.shaker.entity.recipe.GeneratedImageSpec;
import com.shaker.entity.recipe.Glass;
import com.shaker.entity.recipe.Ingredient;
import com.shaker.entity.recipe.IngredientCategory;
import com.shaker.entity.recipe.IngredientLine;
import com.shaker.entity.recipe.IngredientRole;
import com.shaker.entity.recipe.MeasurementUnit;
import com.shaker.entity.recipe.Opacity;
import com.shaker.entity.recipe.PreparationMethod;
import com.shaker.entity.recipe.Recipe;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Everything about a recipe that's derived from its ingredients rather than entered directly:
 * ABV estimate, calorie estimate, base-spirit tags, dietary flags, and the generated glass
 * illustration spec. Kept out of {@link RecipeService} so this math is unit-testable on its
 * own, without mocking a repository.
 */
@Component
public class RecipeCompositionCalculator {

    private static final double ML_PER_OZ = 29.5735;
    private static final double ETHANOL_DENSITY_G_PER_ML = 0.789;
    private static final double KCAL_PER_GRAM_ALCOHOL = 7.0;
    /** kcal per fluid oz of pure ethanol - folds the constants above into one factor. */
    private static final double KCAL_PER_OZ_PURE_ALCOHOL = ML_PER_OZ * ETHANOL_DENSITY_G_PER_ML * KCAL_PER_GRAM_ALCOHOL;

    private static final Map<Glass, Double> GLASS_CAPACITY_OZ = Map.ofEntries(
            Map.entry(Glass.COUPE, 5.0),
            Map.entry(Glass.ROCKS, 8.0),
            Map.entry(Glass.HIGHBALL, 10.0),
            Map.entry(Glass.COLLINS, 12.0),
            Map.entry(Glass.MARTINI, 6.0),
            Map.entry(Glass.NICK_AND_NORA, 5.0),
            Map.entry(Glass.HURRICANE, 16.0),
            Map.entry(Glass.COPPER_MUG, 12.0),
            Map.entry(Glass.FLUTE, 6.0),
            Map.entry(Glass.SNIFTER, 8.0),
            Map.entry(Glass.TIKI_MUG, 14.0),
            Map.entry(Glass.WINE, 8.0),
            Map.entry(Glass.SHOT, 2.0));

    /** Applies every derived field onto {@code recipe} in place, based on its ingredient lines. */
    public void apply(Recipe recipe, Map<String, Ingredient> ingredientsById) {
        List<IngredientLine> lines = recipe.getIngredients();

        recipe.setAbvEstimate(computeAbvEstimate(lines, ingredientsById));
        recipe.setBaseSpiritTags(computeBaseSpiritTags(lines, ingredientsById));
        recipe.setDietaryFlags(computeDietaryFlags(lines, ingredientsById));

        if (recipe.getCalorieSource() != CalorieSource.USER_ENTERED) {
            recipe.setCalorieEstimate(computeCalorieEstimate(lines, ingredientsById));
            recipe.setCalorieSource(CalorieSource.CALCULATED);
        }

        recipe.setGeneratedImageSpec(buildImageSpec(recipe, lines, ingredientsById));
    }

    double computeAbvEstimate(List<IngredientLine> lines, Map<String, Ingredient> ingredientsById) {
        double totalOz = 0;
        double alcoholOz = 0;
        for (IngredientLine line : lines) {
            double oz = toOunces(line.getAmount(), line.getUnit());
            totalOz += oz;
            Ingredient ingredient = resolve(line, ingredientsById);
            if (ingredient != null) {
                alcoholOz += oz * (ingredient.getAbvPercent() / 100.0);
            }
        }
        if (totalOz <= 0) {
            return 0.0;
        }
        return Math.round((alcoholOz / totalOz) * 1000.0) / 10.0; // one decimal place
    }

    Integer computeCalorieEstimate(List<IngredientLine> lines, Map<String, Ingredient> ingredientsById) {
        boolean anyResolved = false;
        double totalCalories = 0;
        for (IngredientLine line : lines) {
            Ingredient ingredient = resolve(line, ingredientsById);
            if (ingredient == null) {
                continue;
            }
            anyResolved = true;
            double oz = toOunces(line.getAmount(), line.getUnit());
            totalCalories += oz * (ingredient.getAbvPercent() / 100.0) * KCAL_PER_OZ_PURE_ALCOHOL;
            if (ingredient.getCaloriesPerOz() != null) {
                totalCalories += oz * ingredient.getCaloriesPerOz();
            }
        }
        return anyResolved ? (int) Math.round(totalCalories) : null;
    }

    Set<String> computeBaseSpiritTags(List<IngredientLine> lines, Map<String, Ingredient> ingredientsById) {
        Set<String> tags = new LinkedHashSet<>();
        for (IngredientLine line : lines) {
            if (line.getRole() != IngredientRole.BASE_SPIRIT) {
                continue;
            }
            Ingredient ingredient = resolve(line, ingredientsById);
            if (ingredient == null || ingredient.getCategory() != IngredientCategory.SPIRIT) {
                continue;
            }
            String tag = ingredient.getSubCategory() != null ? ingredient.getSubCategory() : ingredient.getName();
            tags.add(tag.trim().toLowerCase());
        }
        return tags;
    }

    Set<String> computeDietaryFlags(List<IngredientLine> lines, Map<String, Ingredient> ingredientsById) {
        Set<String> allergens = new LinkedHashSet<>();
        boolean anyDairyEggCategory = false;
        for (IngredientLine line : lines) {
            Ingredient ingredient = resolve(line, ingredientsById);
            if (ingredient == null) {
                continue;
            }
            allergens.addAll(ingredient.getAllergenTags());
            if (ingredient.getCategory() == IngredientCategory.DAIRY_EGG) {
                anyDairyEggCategory = true;
            }
        }

        Set<String> flags = new LinkedHashSet<>();
        if (!allergens.contains("dairy")) {
            flags.add("dairy-free");
        }
        if (!allergens.contains("egg")) {
            flags.add("egg-free");
        }
        if (!allergens.contains("gluten")) {
            flags.add("gluten-free");
        }
        if (!allergens.contains("nuts")) {
            flags.add("nut-free");
        }
        boolean vegan = !anyDairyEggCategory
                && !allergens.contains("dairy") && !allergens.contains("egg") && !allergens.contains("honey");
        if (vegan) {
            flags.add("vegan");
        }
        return flags;
    }

    GeneratedImageSpec buildImageSpec(Recipe recipe, List<IngredientLine> lines, Map<String, Ingredient> ingredientsById) {
        GeneratedImageSpec spec = new GeneratedImageSpec();
        spec.setGlass(recipe.getGlass());
        spec.setIceOverlay(recipe.getIce());
        spec.setCarbonationOverlay(lines.stream()
                .map(line -> resolve(line, ingredientsById))
                .filter(java.util.Objects::nonNull)
                .anyMatch(i -> i.getOpacity() == Opacity.CARBONATED));
        spec.setGarnishIcon(recipe.getGarnishes().isEmpty() ? null : recipe.getGarnishes().get(0).getType());

        double totalOz = lines.stream().mapToDouble(l -> toOunces(l.getAmount(), l.getUnit())).sum();
        double capacity = GLASS_CAPACITY_OZ.getOrDefault(recipe.getGlass(), 8.0);
        spec.setFillLevel(totalOz <= 0 ? 0 : Math.min(1.0, totalOz / capacity));

        if (totalOz <= 0) {
            return spec;
        }

        if (recipe.getMethod() == PreparationMethod.LAYERED) {
            List<IngredientLine> ordered = lines.stream()
                    .sorted(Comparator.comparingDouble((IngredientLine l) -> {
                        Ingredient ingredient = resolve(l, ingredientsById);
                        Double density = ingredient != null ? ingredient.getRelativeDensity() : null;
                        return density != null ? density : 0.0;
                    }).reversed())
                    .toList();
            for (IngredientLine line : ordered) {
                Ingredient ingredient = resolve(line, ingredientsById);
                double oz = toOunces(line.getAmount(), line.getUnit());
                ColorBand band = new ColorBand();
                band.setColorHex(ingredient != null && ingredient.getColorHex() != null ? ingredient.getColorHex() : "#CCCCCC");
                band.setProportion(oz / totalOz);
                spec.getColorBands().add(band);
            }
        } else {
            ColorBand blended = new ColorBand();
            blended.setColorHex(blendColors(lines, ingredientsById, totalOz));
            blended.setProportion(1.0);
            spec.getColorBands().add(blended);
        }
        return spec;
    }

    private String blendColors(List<IngredientLine> lines, Map<String, Ingredient> ingredientsById, double totalOz) {
        double r = 0;
        double g = 0;
        double b = 0;
        for (IngredientLine line : lines) {
            Ingredient ingredient = resolve(line, ingredientsById);
            String hex = ingredient != null && ingredient.getColorHex() != null ? ingredient.getColorHex() : "#CCCCCC";
            double weight = toOunces(line.getAmount(), line.getUnit()) / totalOz;
            int[] rgb = hexToRgb(hex);
            r += rgb[0] * weight;
            g += rgb[1] * weight;
            b += rgb[2] * weight;
        }
        return rgbToHex((int) Math.round(r), (int) Math.round(g), (int) Math.round(b));
    }

    private int[] hexToRgb(String hex) {
        String clean = hex.startsWith("#") ? hex.substring(1) : hex;
        return new int[]{
                Integer.parseInt(clean.substring(0, 2), 16),
                Integer.parseInt(clean.substring(2, 4), 16),
                Integer.parseInt(clean.substring(4, 6), 16)
        };
    }

    private String rgbToHex(int r, int g, int b) {
        return String.format("#%02X%02X%02X",
                Math.clamp(r, 0, 255), Math.clamp(g, 0, 255), Math.clamp(b, 0, 255));
    }

    private Ingredient resolve(IngredientLine line, Map<String, Ingredient> ingredientsById) {
        return line.getIngredientRef() != null ? ingredientsById.get(line.getIngredientRef()) : null;
    }

    private double toOunces(Double amount, MeasurementUnit unit) {
        if (amount == null || unit == null) {
            return 0;
        }
        return switch (unit) {
            case OZ -> amount;
            case ML -> amount / ML_PER_OZ;
            case CL -> (amount * 10) / ML_PER_OZ;
            case DASH -> amount * 0.02;
            case BARSPOON -> amount * 0.166;
            default -> 0; // DROP, PART, PIECE, LEAF, SPRIG, WEDGE, PINCH - not volumetric
        };
    }
}
