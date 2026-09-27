package com.shaker.service;

import com.shaker.dto.IngredientLineRequestDto;
import com.shaker.dto.RecipeAvailabilityDto;
import com.shaker.dto.RecipeRequestDto;
import com.shaker.entity.bar.BarItemType;
import com.shaker.entity.bar.UserBarItem;
import com.shaker.entity.common.Visibility;
import com.shaker.entity.recipe.Glass;
import com.shaker.entity.recipe.IceStyle;
import com.shaker.entity.recipe.Ingredient;
import com.shaker.entity.recipe.IngredientRole;
import com.shaker.entity.recipe.MeasurementUnit;
import com.shaker.entity.recipe.PreparationMethod;
import com.shaker.entity.recipe.Recipe;
import com.shaker.entity.recipe.RecipeOrigin;
import com.shaker.exception.NotFoundException;
import com.shaker.repository.IngredientRepository;
import com.shaker.repository.RecipeRepository;
import com.shaker.repository.UserBarItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    RecipeRepository recipes;

    @Mock
    IngredientRepository ingredients;

    @Mock
    UserBarItemRepository barItems;

    @Mock
    RecipeCompositionCalculator calculator;

    @InjectMocks
    RecipeService recipeService;

    private static RecipeRequestDto request(String name, Visibility visibility) {
        return new RecipeRequestDto(name, null, Set.of(), Glass.ROCKS, IceStyle.ROCKS_CUBED, PreparationMethod.STIRRED,
                List.of(new IngredientLineRequestDto("gin-id", null, IngredientRole.BASE_SPIRIT, 1.0, MeasurementUnit.OZ,
                        null, false, 1)),
                List.of(), List.of(), 1, Set.of(), null, Set.of(), null, List.of(), visibility, null);
    }

    private static Recipe recipeWith(String id, String createdBy, Visibility visibility) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setCreatedBy(createdBy);
        recipe.setVisibility(visibility);
        recipe.setIngredients(List.of());
        return recipe;
    }

    @Test
    void create_is_a_fresh_original_owned_by_the_caller() {
        when(recipes.save(any(Recipe.class))).thenAnswer(inv -> inv.getArgument(0));

        Recipe result = recipeService.create("alice", request("Martini", Visibility.PRIVATE));

        assertThat(result.getOrigin()).isEqualTo(RecipeOrigin.USER_ORIGINAL);
        assertThat(result.getCreatedBy()).isEqualTo("alice");
        assertThat(result.getVersion()).isEqualTo(1);
        assertThat(result.getRecipeFamilyId()).isNull();
        assertThat(result.getForkedFromRecipeId()).isNull();
    }

    @Test
    void createNewVersion_assigns_a_family_id_when_the_source_has_none() {
        Recipe source = recipeWith("r1", "alice", Visibility.PRIVATE);
        source.setVersion(1);
        when(recipes.findByIdAndCreatedBy("r1", "alice")).thenReturn(Optional.of(source));
        when(recipes.findByRecipeFamilyIdOrderByVersionDesc(any())).thenReturn(List.of());
        when(recipes.save(any(Recipe.class))).thenAnswer(inv -> inv.getArgument(0));

        Recipe version = recipeService.createNewVersion("alice", "r1", request("Martini v2", Visibility.PRIVATE));

        assertThat(source.getRecipeFamilyId()).isNotNull(); // backfilled onto the source too
        assertThat(version.getRecipeFamilyId()).isEqualTo(source.getRecipeFamilyId());
        assertThat(version.getVersion()).isEqualTo(2);
        assertThat(version.getCreatedBy()).isEqualTo("alice");
    }

    @Test
    void createNewVersion_continues_the_sequence_when_a_family_already_exists() {
        Recipe source = recipeWith("r1", "alice", Visibility.PRIVATE);
        source.setRecipeFamilyId("fam-1");
        source.setVersion(2);
        Recipe latest = recipeWith("r2", "alice", Visibility.PRIVATE);
        latest.setRecipeFamilyId("fam-1");
        latest.setVersion(3);
        when(recipes.findByIdAndCreatedBy("r1", "alice")).thenReturn(Optional.of(source));
        when(recipes.findByRecipeFamilyIdOrderByVersionDesc("fam-1")).thenReturn(List.of(latest, source));
        when(recipes.save(any(Recipe.class))).thenAnswer(inv -> inv.getArgument(0));

        Recipe version = recipeService.createNewVersion("alice", "r1", request("Martini v4", Visibility.PRIVATE));

        assertThat(version.getVersion()).isEqualTo(4);
        assertThat(version.getRecipeFamilyId()).isEqualTo("fam-1");
    }

    @Test
    void createNewVersion_rejects_a_non_owner() {
        when(recipes.findByIdAndCreatedBy("r1", "mallory")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.createNewVersion("mallory", "r1", request("x", Visibility.PRIVATE)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void fork_starts_a_new_lineage_even_when_the_source_has_a_family() {
        Recipe source = recipeWith("classic-1", null, Visibility.PUBLIC);
        source.setRecipeFamilyId("classics-family");
        when(recipes.findById("classic-1")).thenReturn(Optional.of(source));
        when(recipes.save(any(Recipe.class))).thenAnswer(inv -> inv.getArgument(0));

        Recipe fork = recipeService.fork("bob", "classic-1", request("Bob's Twist", Visibility.PRIVATE));

        assertThat(fork.getOrigin()).isEqualTo(RecipeOrigin.USER_FORK);
        assertThat(fork.getCreatedBy()).isEqualTo("bob");
        assertThat(fork.getForkedFromRecipeId()).isEqualTo("classic-1");
        assertThat(fork.getVersion()).isEqualTo(1);
        assertThat(fork.getRecipeFamilyId()).isNull(); // new lineage, not the source's family
    }

    @Test
    void update_and_delete_reject_a_non_owner() {
        when(recipes.findByIdAndCreatedBy("r1", "mallory")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.update("mallory", "r1", request("x", Visibility.PRIVATE)))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> recipeService.delete("mallory", "r1"))
                .isInstanceOf(NotFoundException.class);
        verify(recipes, org.mockito.Mockito.never()).delete(any());
    }

    @Test
    void getVisible_allows_anyone_to_see_a_public_recipe() {
        when(recipes.findById("r1")).thenReturn(Optional.of(recipeWith("r1", "alice", Visibility.PUBLIC)));

        assertThat(recipeService.getVisible("r1", null).getId()).isEqualTo("r1");
    }

    @Test
    void getVisible_hides_a_private_recipe_from_non_owners() {
        when(recipes.findById("r1")).thenReturn(Optional.of(recipeWith("r1", "alice", Visibility.PRIVATE)));

        assertThatThrownBy(() -> recipeService.getVisible("r1", "bob"))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> recipeService.getVisible("r1", null))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getVisible_shows_a_private_recipe_to_its_owner() {
        when(recipes.findById("r1")).thenReturn(Optional.of(recipeWith("r1", "alice", Visibility.PRIVATE)));

        assertThat(recipeService.getVisible("r1", "alice").getId()).isEqualTo("r1");
    }

    @Test
    void checkAvailability_only_flags_missing_non_optional_catalogued_ingredients() {
        Recipe recipe = recipeWith("r1", "alice", Visibility.PUBLIC);
        var owned = new com.shaker.entity.recipe.IngredientLine();
        owned.setIngredientRef("gin");
        owned.setOptional(false);
        var missingCatalogued = new com.shaker.entity.recipe.IngredientLine();
        missingCatalogued.setIngredientRef("campari");
        missingCatalogued.setOptional(false);
        var optionalMissing = new com.shaker.entity.recipe.IngredientLine();
        optionalMissing.setIngredientRef("bitters");
        optionalMissing.setOptional(true);
        var freeText = new com.shaker.entity.recipe.IngredientLine();
        freeText.setFreeTextName("house-made cordial");
        freeText.setOptional(false);
        recipe.setIngredients(List.of(owned, missingCatalogued, optionalMissing, freeText));

        when(recipes.findById("r1")).thenReturn(Optional.of(recipe));
        UserBarItem ginItem = new UserBarItem();
        ginItem.setItemType(BarItemType.INGREDIENT);
        ginItem.setIngredientRef("gin");
        when(barItems.findByOwnerUsername("alice")).thenReturn(List.of(ginItem));
        Ingredient campariIngredient = new Ingredient();
        campariIngredient.setId("campari");
        campariIngredient.setName("Campari");
        when(ingredients.findAllById(any())).thenReturn(List.of(campariIngredient));

        RecipeAvailabilityDto availability = recipeService.checkAvailability("r1", "alice");

        assertThat(availability.hasAllIngredients()).isFalse();
        assertThat(availability.missingIngredientNames()).containsExactly("Campari");
    }

    @Test
    void createClassic_is_owner_less_and_forced_public() {
        when(recipes.save(any(Recipe.class))).thenAnswer(inv -> inv.getArgument(0));

        Recipe classic = recipeService.createClassic(request("Gin Tonic", Visibility.PRIVATE));

        assertThat(classic.getOrigin()).isEqualTo(RecipeOrigin.APP_CLASSIC);
        assertThat(classic.getCreatedBy()).isNull();
        assertThat(classic.getVisibility()).isEqualTo(Visibility.PUBLIC); // ignores the requested PRIVATE
    }

    @Test
    void updateVisibility_overrides_regardless_of_ownership() {
        Recipe recipe = recipeWith("r1", "alice", Visibility.PUBLIC);
        when(recipes.findById("r1")).thenReturn(Optional.of(recipe));
        when(recipes.save(any(Recipe.class))).thenAnswer(inv -> inv.getArgument(0));

        Recipe updated = recipeService.updateVisibility("r1", Visibility.PRIVATE);

        assertThat(updated.getVisibility()).isEqualTo(Visibility.PRIVATE);
    }

    @Test
    void updateOwnVisibility_requires_ownership() {
        Recipe recipe = recipeWith("r1", "alice", Visibility.PUBLIC);
        when(recipes.findByIdAndCreatedBy("r1", "alice")).thenReturn(Optional.of(recipe));
        when(recipes.findByIdAndCreatedBy("r1", "mallory")).thenReturn(Optional.empty());
        when(recipes.save(any(Recipe.class))).thenAnswer(inv -> inv.getArgument(0));

        Recipe updated = recipeService.updateOwnVisibility("alice", "r1", Visibility.PRIVATE);
        assertThat(updated.getVisibility()).isEqualTo(Visibility.PRIVATE);

        assertThatThrownBy(() -> recipeService.updateOwnVisibility("mallory", "r1", Visibility.PRIVATE))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findAllForAdmin_returns_every_recipe_regardless_of_visibility() {
        when(recipes.findAll()).thenReturn(List.of(recipeWith("r1", "alice", Visibility.PRIVATE)));

        assertThat(recipeService.findAllForAdmin()).hasSize(1);
    }
}
