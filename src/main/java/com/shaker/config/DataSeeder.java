package com.shaker.config;

import com.shaker.entity.common.Visibility;
import com.shaker.entity.guideline.Guideline;
import com.shaker.entity.recipe.Difficulty;
import com.shaker.entity.recipe.Garnish;
import com.shaker.entity.recipe.GarnishType;
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
import com.shaker.entity.recipe.RecipeCategory;
import com.shaker.entity.recipe.RecipeOrigin;
import com.shaker.entity.recipe.TasteNote;
import com.shaker.entity.user.Role;
import com.shaker.entity.user.User;
import com.shaker.repository.GuidelineRepository;
import com.shaker.repository.IngredientRepository;
import com.shaker.repository.RecipeRepository;
import com.shaker.repository.UserRepository;
import com.shaker.service.RecipeCompositionCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Idempotent seed data for the configured database. Disable entirely with
 * {@code app.seed=false}. The admin user is only created when {@code app.admin-password}
 * is set, so a fresh cluster never gets weak default credentials.
 */
@Component
@ConditionalOnProperty(name = "app.seed", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository users;
    private final GuidelineRepository guidelines;
    private final IngredientRepository ingredients;
    private final RecipeRepository recipes;
    private final RecipeCompositionCalculator calculator;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public DataSeeder(UserRepository users, GuidelineRepository guidelines, IngredientRepository ingredients,
                      RecipeRepository recipes, RecipeCompositionCalculator calculator,
                      PasswordEncoder passwordEncoder,
                      @Value("${app.admin-username:admin}") String adminUsername,
                      @Value("${app.admin-password:}") String adminPassword) {
        this.users = users;
        this.guidelines = guidelines;
        this.ingredients = ingredients;
        this.recipes = recipes;
        this.calculator = calculator;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername.trim().toLowerCase();
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedGuidelines();
        seedIngredientsAndClassicRecipes();
    }

    private void seedAdmin() {
        if (!StringUtils.hasText(adminPassword)) {
            log.info("app.admin-password not set - skipping admin user seed");
            return;
        }
        if (users.existsByUsername(adminUsername)) {
            return;
        }
        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setEmail(adminUsername + "@shaker.local");
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setDisplayName("Shaker Admin");
        admin.setRoles(new LinkedHashSet<>(Set.of(Role.USER, Role.ADMIN)));
        admin.setCreatedAt(Instant.now());
        users.save(admin);
        log.info("Seeded admin user '{}'", adminUsername);
    }

    private void seedGuidelines() {
        if (guidelines.count() > 0) {
            return;
        }
        guidelines.saveAll(List.of(
                guideline("bar-basics", "Bar Basics", "Fundamentals", 1,
                        "Chill your glassware. Use fresh citrus. Measure everything - "
                                + "consistency is what makes a drink repeatable."),
                guideline("shake-vs-stir", "Shake vs. Stir", "Technique", 2,
                        "Stir drinks that are all spirits (Martini, Negroni, Manhattan). "
                                + "Shake anything with juice, dairy, or egg."),
                guideline("measurements", "Measurement Conversions", "Reference", 3,
                        "1 oz = 30 ml. 1 dash ~= 0.8 ml. A standard cocktail is 2 oz spirit, "
                                + "0.75 oz citrus, 0.75 oz sweetener.")
        ));
        log.info("Seeded {} guidelines", guidelines.count());
    }

    private static Guideline guideline(String slug, String title, String category, int order, String body) {
        Guideline g = new Guideline();
        g.setSlug(slug);
        g.setTitle(title);
        g.setCategory(category);
        g.setSortOrder(order);
        g.setBody(body);
        return g;
    }

    /** A handful of common ingredients plus three IBA classics built from them, so the recipe
     *  feature (ABV/calorie estimates, spirit-tag filtering, "you have all ingredients") is
     *  demoable out of the box. */
    private void seedIngredientsAndClassicRecipes() {
        if (ingredients.count() > 0 || recipes.count() > 0) {
            return;
        }

        Ingredient gin = ingredients.save(ingredient("Gin", IngredientCategory.SPIRIT, "gin",
                40, "#F4F1E8", Opacity.CLEAR, null, Set.of(), 64.0));
        Ingredient whiteRum = ingredients.save(ingredient("White Rum", IngredientCategory.SPIRIT, "white_rum",
                40, "#F4F1E8", Opacity.CLEAR, null, Set.of(), 64.0));
        Ingredient ryeWhiskey = ingredients.save(ingredient("Rye Whiskey", IngredientCategory.SPIRIT, "rye_whiskey",
                45, "#B5651D", Opacity.TRANSLUCENT, null, Set.of(), 70.0));
        Ingredient sweetVermouth = ingredients.save(ingredient("Sweet Vermouth", IngredientCategory.VERMOUTH, "sweet_vermouth",
                16, "#7B3F00", Opacity.TRANSLUCENT, 1.05, Set.of(), 30.0));
        Ingredient campari = ingredients.save(ingredient("Campari", IngredientCategory.LIQUEUR, "amaro",
                24, "#D7263D", Opacity.TRANSLUCENT, 1.08, Set.of(), 20.0));
        Ingredient simpleSyrup = ingredients.save(ingredient("Simple Syrup", IngredientCategory.SYRUP, null,
                0, "#FFFFFF", Opacity.CLEAR, 1.3, Set.of(), 64.0));
        Ingredient angosturaBitters = ingredients.save(ingredient("Angostura Bitters", IngredientCategory.BITTERS, null,
                44.7, "#6E1F0C", Opacity.TRANSLUCENT, null, Set.of(), 2.0));
        Ingredient limeJuice = ingredients.save(ingredient("Lime Juice", IngredientCategory.JUICE, null,
                0, "#C6E377", Opacity.TRANSLUCENT, 1.02, Set.of(), 8.0));

        Map<String, Ingredient> byId = List.of(gin, whiteRum, ryeWhiskey, sweetVermouth, campari,
                        simpleSyrup, angosturaBitters, limeJuice).stream()
                .collect(Collectors.toMap(Ingredient::getId, Function.identity()));

        Recipe negroni = classicRecipe("Negroni", RecipeCategory.UNFORGETTABLES, Glass.ROCKS, IceStyle.ROCKS_CUBED,
                PreparationMethod.STIRRED, Set.of(TasteNote.BITTER, TasteNote.STRONG, TasteNote.DRY),
                "Equal parts gin, sweet vermouth, and Campari - stirred, never shaken.",
                List.of(
                        line(gin, IngredientRole.BASE_SPIRIT, 1.0, MeasurementUnit.OZ, 1),
                        line(sweetVermouth, IngredientRole.MODIFIER, 1.0, MeasurementUnit.OZ, 2),
                        line(campari, IngredientRole.MODIFIER, 1.0, MeasurementUnit.OZ, 3)),
                garnish("Orange twist", GarnishType.CITRUS_TWIST));

        Recipe oldFashioned = classicRecipe("Old Fashioned", RecipeCategory.UNFORGETTABLES, Glass.ROCKS, IceStyle.ROCKS_ONE_BIG_ROCK,
                PreparationMethod.BUILT, Set.of(TasteNote.STRONG, TasteNote.SWEET, TasteNote.BITTER),
                "Whiskey, sugar, and bitters, built over one big rock.",
                List.of(
                        line(ryeWhiskey, IngredientRole.BASE_SPIRIT, 2.0, MeasurementUnit.OZ, 1),
                        line(simpleSyrup, IngredientRole.SWEETENER, 0.25, MeasurementUnit.OZ, 2),
                        line(angosturaBitters, IngredientRole.BITTERS, 2.0, MeasurementUnit.DASH, 3)),
                garnish("Orange twist", GarnishType.CITRUS_TWIST));

        Recipe daiquiri = classicRecipe("Daiquiri", RecipeCategory.UNFORGETTABLES, Glass.COUPE, IceStyle.UP,
                PreparationMethod.SHAKEN, Set.of(TasteNote.SOUR, TasteNote.REFRESHING, TasteNote.FRUITY),
                "Rum, lime, and sugar, shaken hard and strained up.",
                List.of(
                        line(whiteRum, IngredientRole.BASE_SPIRIT, 2.0, MeasurementUnit.OZ, 1),
                        line(limeJuice, IngredientRole.MIXER, 1.0, MeasurementUnit.OZ, 2),
                        line(simpleSyrup, IngredientRole.SWEETENER, 0.75, MeasurementUnit.OZ, 3)),
                garnish("Lime wheel", GarnishType.CITRUS_WHEEL));

        for (Recipe recipe : List.of(negroni, oldFashioned, daiquiri)) {
            calculator.apply(recipe, byId);
        }
        recipes.saveAll(List.of(negroni, oldFashioned, daiquiri));
        log.info("Seeded {} ingredients and {} classic recipes", ingredients.count(), recipes.count());
    }

    private static Ingredient ingredient(String name, IngredientCategory category, String subCategory,
                                         double abvPercent, String colorHex, Opacity opacity,
                                         Double relativeDensity, Set<String> allergenTags, Double caloriesPerOz) {
        Ingredient i = new Ingredient();
        i.setName(name);
        i.setCategory(category);
        i.setSubCategory(subCategory);
        i.setAbvPercent(abvPercent);
        i.setColorHex(colorHex);
        i.setOpacity(opacity);
        i.setRelativeDensity(relativeDensity);
        i.setAllergenTags(allergenTags);
        i.setCaloriesPerOz(caloriesPerOz);
        return i;
    }

    private static IngredientLine line(Ingredient ingredient, IngredientRole role, double amount,
                                       MeasurementUnit unit, int sequence) {
        IngredientLine line = new IngredientLine();
        line.setIngredientRef(ingredient.getId());
        line.setRole(role);
        line.setAmount(amount);
        line.setUnit(unit);
        line.setSequence(sequence);
        return line;
    }

    private static Garnish garnish(String description, GarnishType type) {
        Garnish g = new Garnish();
        g.setDescription(description);
        g.setType(type);
        return g;
    }

    private static Recipe classicRecipe(String name, RecipeCategory category, Glass glass, IceStyle ice,
                                        PreparationMethod method, Set<TasteNote> tasteProfile, String description,
                                        List<IngredientLine> ingredientLines, Garnish garnish) {
        Recipe recipe = new Recipe();
        recipe.setName(name);
        recipe.setOrigin(RecipeOrigin.APP_CLASSIC);
        recipe.setCreatedBy(null);
        recipe.setVisibility(Visibility.PUBLIC);
        recipe.setCategory(category);
        recipe.setGlass(glass);
        recipe.setIce(ice);
        recipe.setMethod(method);
        recipe.setTasteProfile(tasteProfile);
        recipe.setDifficulty(Difficulty.EASY);
        recipe.setDescription(description);
        recipe.setIngredients(ingredientLines);
        recipe.setGarnishes(List.of(garnish));
        recipe.setInstructions(List.of(
                "Combine all ingredients.",
                method == PreparationMethod.SHAKEN ? "Shake hard with ice and double-strain."
                        : "Stir with ice until well-chilled and strain (or build directly in the glass)."));
        return recipe;
    }
}
