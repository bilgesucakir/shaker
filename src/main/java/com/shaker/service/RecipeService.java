package com.shaker.service;

import com.shaker.dto.GarnishRequestDto;
import com.shaker.dto.IngredientLineRequestDto;
import com.shaker.dto.RecipeAvailabilityDto;
import com.shaker.dto.RecipeRequestDto;
import com.shaker.entity.bar.BarItemType;
import com.shaker.entity.bar.UserBarItem;
import com.shaker.entity.common.Visibility;
import com.shaker.entity.recipe.CalorieSource;
import com.shaker.entity.recipe.Garnish;
import com.shaker.entity.recipe.Ingredient;
import com.shaker.entity.recipe.IngredientLine;
import com.shaker.entity.recipe.Recipe;
import com.shaker.entity.recipe.RecipeOrigin;
import com.shaker.exception.NotFoundException;
import com.shaker.repository.IngredientRepository;
import com.shaker.repository.RecipeRepository;
import com.shaker.repository.UserBarItemRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class RecipeService {

    private final RecipeRepository recipes;
    private final IngredientRepository ingredients;
    private final UserBarItemRepository barItems;
    private final RecipeCompositionCalculator calculator;

    public RecipeService(RecipeRepository recipes, IngredientRepository ingredients,
                         UserBarItemRepository barItems, RecipeCompositionCalculator calculator) {
        this.recipes = recipes;
        this.ingredients = ingredients;
        this.barItems = barItems;
        this.calculator = calculator;
    }

    public Recipe create(String username, RecipeRequestDto request) {
        Recipe recipe = new Recipe();
        recipe.setOrigin(RecipeOrigin.USER_ORIGINAL);
        recipe.setCreatedBy(username);
        recipe.setVersion(1);
        applyComposed(recipe, request);
        return recipes.save(recipe);
    }

    /** Saves an edited copy of the caller's own recipe as the next version in its family. */
    public Recipe createNewVersion(String username, String sourceId, RecipeRequestDto request) {
        Recipe source = getOwned(sourceId, username);

        String familyId = source.getRecipeFamilyId();
        if (familyId == null) {
            familyId = UUID.randomUUID().toString();
            source.setRecipeFamilyId(familyId);
            recipes.save(source);
        }

        int nextVersion = recipes.findByRecipeFamilyIdOrderByVersionDesc(familyId).stream()
                .findFirst().map(Recipe::getVersion).orElse(source.getVersion()) + 1;

        Recipe version = new Recipe();
        version.setOrigin(source.getOrigin());
        version.setCreatedBy(username);
        version.setRecipeFamilyId(familyId);
        version.setForkedFromRecipeId(source.getForkedFromRecipeId());
        version.setVersion(nextVersion);
        applyComposed(version, request);
        return recipes.save(version);
    }

    /** Starts a brand-new recipe lineage from any visible recipe (a classic or someone else's). */
    public Recipe fork(String username, String sourceId, RecipeRequestDto request) {
        Recipe source = getVisible(sourceId, username);

        Recipe forked = new Recipe();
        forked.setOrigin(RecipeOrigin.USER_FORK);
        forked.setCreatedBy(username);
        forked.setForkedFromRecipeId(source.getId());
        forked.setVersion(1);
        applyComposed(forked, request);
        return recipes.save(forked);
    }

    /** Edits a recipe in place - same version/family, just updated content. Owner only. */
    public Recipe update(String username, String id, RecipeRequestDto request) {
        Recipe recipe = getOwned(id, username);
        applyComposed(recipe, request);
        recipe.setUpdatedAt(Instant.now());
        return recipes.save(recipe);
    }

    public void delete(String username, String id) {
        recipes.delete(getOwned(id, username));
    }

    /** Admin-authored classics (gin tonic, Aperol spritz, ...) - always public, no owner. */
    public Recipe createClassic(RecipeRequestDto request) {
        Recipe recipe = new Recipe();
        recipe.setOrigin(RecipeOrigin.APP_CLASSIC);
        recipe.setCreatedBy(null);
        recipe.setVersion(1);
        applyComposed(recipe, request);
        recipe.setVisibility(Visibility.PUBLIC);
        return recipes.save(recipe);
    }

    /** Admin oversight - every recipe regardless of visibility. */
    public List<Recipe> findAllForAdmin() {
        return recipes.findAll();
    }

    /** Admin override of a recipe's visibility, bypassing ownership (e.g. to unpublish). */
    public Recipe updateVisibility(String id, Visibility visibility) {
        Recipe recipe = recipes.findById(id).orElseThrow(() -> new NotFoundException("Recipe not found"));
        recipe.setVisibility(visibility);
        recipe.setUpdatedAt(Instant.now());
        return recipes.save(recipe);
    }

    /** A regular user changing the visibility of their own recipe. */
    public Recipe updateOwnVisibility(String username, String id, Visibility visibility) {
        Recipe recipe = getOwned(id, username);
        recipe.setVisibility(visibility);
        recipe.setUpdatedAt(Instant.now());
        return recipes.save(recipe);
    }

    public Recipe getVisible(String id, String viewerUsername) {
        Recipe recipe = recipes.findById(id).orElseThrow(() -> new NotFoundException("Recipe not found"));
        boolean owner = recipe.getCreatedBy() != null && recipe.getCreatedBy().equals(viewerUsername);
        if (recipe.getVisibility() == Visibility.PRIVATE && !owner) {
            throw new NotFoundException("Recipe not found");
        }
        return recipe;
    }

    public List<Recipe> listPublic(String baseSpiritTag) {
        return baseSpiritTag == null
                ? recipes.findByVisibility(Visibility.PUBLIC)
                : recipes.findByVisibilityAndBaseSpiritTagsContaining(Visibility.PUBLIC, baseSpiritTag.toLowerCase());
    }

    public List<Recipe> listMine(String username) {
        return recipes.findByCreatedBy(username);
    }

    /** "You have all ingredients!" - checks a recipe's non-optional, cataloged ingredients against a user's bar. */
    public RecipeAvailabilityDto checkAvailability(String recipeId, String username) {
        Recipe recipe = getVisible(recipeId, username);

        java.util.Set<String> owned = barItems.findByOwnerUsername(username).stream()
                .filter(item -> item.getItemType() == BarItemType.INGREDIENT)
                .map(UserBarItem::getIngredientRef)
                .collect(java.util.stream.Collectors.toSet());

        Map<String, Ingredient> byId = loadIngredients(recipe.getIngredients());

        List<String> missing = recipe.getIngredients().stream()
                .filter(line -> !line.isOptional())
                .filter(line -> line.getIngredientRef() != null)
                .filter(line -> !owned.contains(line.getIngredientRef()))
                .map(line -> {
                    Ingredient ingredient = byId.get(line.getIngredientRef());
                    return ingredient != null ? ingredient.getName() : line.getIngredientRef();
                })
                .distinct()
                .toList();

        return new RecipeAvailabilityDto(missing.isEmpty(), missing);
    }

    private Recipe getOwned(String id, String username) {
        return recipes.findByIdAndCreatedBy(id, username)
                .orElseThrow(() -> new NotFoundException("Recipe not found"));
    }

    private void applyComposed(Recipe recipe, RecipeRequestDto request) {
        recipe.setName(request.name().trim());
        recipe.setCategory(request.category());
        recipe.setGlass(request.glass());
        recipe.setIce(request.ice());
        recipe.setMethod(request.method());
        recipe.setServings(request.servings() <= 0 ? 1 : request.servings());
        recipe.setTasteProfile(request.tasteProfile() != null ? request.tasteProfile() : java.util.Set.of());
        recipe.setDifficulty(request.difficulty());
        recipe.setTags(request.tags() != null ? request.tags() : java.util.Set.of());
        recipe.setDescription(request.description());
        recipe.setPhotos(request.photos() != null ? request.photos() : List.of());
        recipe.setVisibility(request.visibility());

        recipe.setIngredients(request.ingredients().stream().map(this::toIngredientLine).toList());
        recipe.setGarnishes(request.garnishes() != null
                ? request.garnishes().stream().map(this::toGarnish).toList()
                : List.of());
        recipe.setInstructions(request.instructions() != null ? request.instructions() : List.of());

        if (request.calorieEstimate() != null) {
            recipe.setCalorieEstimate(request.calorieEstimate());
            recipe.setCalorieSource(CalorieSource.USER_ENTERED);
        } else {
            recipe.setCalorieSource(null);
        }

        calculator.apply(recipe, loadIngredients(recipe.getIngredients()));
    }

    private IngredientLine toIngredientLine(IngredientLineRequestDto r) {
        IngredientLine line = new IngredientLine();
        line.setIngredientRef(r.ingredientRef());
        line.setFreeTextName(r.freeTextName());
        line.setRole(r.role());
        line.setAmount(r.amount());
        line.setUnit(r.unit());
        line.setPreparationNote(r.preparationNote());
        line.setOptional(r.optional());
        line.setSequence(r.sequence());
        return line;
    }

    private Garnish toGarnish(GarnishRequestDto r) {
        Garnish garnish = new Garnish();
        garnish.setDescription(r.description());
        garnish.setType(r.type());
        return garnish;
    }

    private Map<String, Ingredient> loadIngredients(List<IngredientLine> lines) {
        List<String> refs = lines.stream().map(IngredientLine::getIngredientRef)
                .filter(java.util.Objects::nonNull).distinct().toList();
        if (refs.isEmpty()) {
            return Map.of();
        }
        return ingredients.findAllById(refs).stream()
                .collect(java.util.stream.Collectors.toMap(Ingredient::getId, Function.identity()));
    }
}
