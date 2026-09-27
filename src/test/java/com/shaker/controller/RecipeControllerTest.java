package com.shaker.controller;

import com.shaker.dto.RecipeAvailabilityDto;
import com.shaker.dto.RecipeRequestDto;
import com.shaker.dto.VisibilityRequestDto;
import com.shaker.entity.common.Visibility;
import com.shaker.entity.recipe.Recipe;
import com.shaker.security.AppUserPrincipal;
import com.shaker.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeControllerTest {

    @Mock
    RecipeService recipeService;

    @Mock
    AppUserPrincipal principal;

    @Mock
    RecipeRequestDto request;

    @InjectMocks
    RecipeController controller;

    @Test
    void getPublicRecipes_delegates_with_the_spirit_filter() {
        Recipe recipe = new Recipe();
        when(recipeService.listPublic("gin")).thenReturn(List.of(recipe));

        assertThat(controller.getPublicRecipes(null, "gin", null)).containsExactly(recipe);
    }

    @Test
    void getPublicRecipes_all_flag_is_ignored_for_anonymous_callers() {
        Recipe recipe = new Recipe();
        when(recipeService.listPublic(null)).thenReturn(List.of(recipe));

        assertThat(controller.getPublicRecipes(null, null, true)).containsExactly(recipe);
    }

    @Test
    void getPublicRecipes_all_flag_is_ignored_for_non_admin_callers() {
        when(principal.isAdmin()).thenReturn(false);
        Recipe recipe = new Recipe();
        when(recipeService.listPublic(null)).thenReturn(List.of(recipe));

        assertThat(controller.getPublicRecipes(principal, null, true)).containsExactly(recipe);
        verify(recipeService, never()).findAllForAdmin();
    }

    @Test
    void getPublicRecipes_all_flag_lists_everything_for_admins() {
        when(principal.isAdmin()).thenReturn(true);
        Recipe recipe = new Recipe();
        when(recipeService.findAllForAdmin()).thenReturn(List.of(recipe));

        assertThat(controller.getPublicRecipes(principal, null, true)).containsExactly(recipe);
    }

    @Test
    void getRecipe_passes_null_username_when_anonymous() {
        Recipe recipe = new Recipe();
        when(recipeService.getVisible("r1", null)).thenReturn(recipe);

        assertThat(controller.getRecipe(null, "r1")).isSameAs(recipe);
    }

    @Test
    void getRecipe_passes_the_callers_username_when_authenticated() {
        when(principal.getUsername()).thenReturn("alice");
        Recipe recipe = new Recipe();
        when(recipeService.getVisible("r1", "alice")).thenReturn(recipe);

        assertThat(controller.getRecipe(principal, "r1")).isSameAs(recipe);
    }

    @Test
    void getAvailability_delegates_to_the_service() {
        when(principal.getUsername()).thenReturn("alice");
        RecipeAvailabilityDto availability = new RecipeAvailabilityDto(true, List.of());
        when(recipeService.checkAvailability("r1", "alice")).thenReturn(availability);

        assertThat(controller.getAvailability(principal, "r1")).isSameAs(availability);
    }

    @Test
    void createRecipe_uses_the_callers_username_by_default() {
        when(principal.getUsername()).thenReturn("alice");
        Recipe recipe = new Recipe();
        when(recipeService.create("alice", request)).thenReturn(recipe);

        assertThat(controller.createRecipe(principal, null, request)).isSameAs(recipe);
    }

    @Test
    void createRecipe_as_classic_is_ignored_for_non_admins() {
        when(principal.getUsername()).thenReturn("alice");
        when(principal.isAdmin()).thenReturn(false);
        Recipe recipe = new Recipe();
        when(recipeService.create("alice", request)).thenReturn(recipe);

        assertThat(controller.createRecipe(principal, true, request)).isSameAs(recipe);
        verify(recipeService, never()).createClassic(request);
    }

    @Test
    void createRecipe_as_classic_creates_an_owner_less_classic_for_admins() {
        when(principal.isAdmin()).thenReturn(true);
        Recipe classic = new Recipe();
        when(recipeService.createClassic(request)).thenReturn(classic);

        assertThat(controller.createRecipe(principal, true, request)).isSameAs(classic);
    }

    @Test
    void forkRecipe_delegates_with_owner_and_source_id() {
        when(principal.getUsername()).thenReturn("bob");
        Recipe fork = new Recipe();
        when(recipeService.fork("bob", "classic-1", request)).thenReturn(fork);

        assertThat(controller.forkRecipe(principal, "classic-1", request)).isSameAs(fork);
    }

    @Test
    void createVersion_delegates_with_owner_and_source_id() {
        when(principal.getUsername()).thenReturn("alice");
        Recipe version = new Recipe();
        when(recipeService.createNewVersion("alice", "r1", request)).thenReturn(version);

        assertThat(controller.createVersion(principal, "r1", request)).isSameAs(version);
    }

    @Test
    void updateVisibility_uses_owner_scoped_update_for_non_admins() {
        when(principal.isAdmin()).thenReturn(false);
        when(principal.getUsername()).thenReturn("alice");
        Recipe recipe = new Recipe();
        when(recipeService.updateOwnVisibility("alice", "r1", Visibility.PRIVATE)).thenReturn(recipe);

        assertThat(controller.updateVisibility(principal, "r1", new VisibilityRequestDto(Visibility.PRIVATE)))
                .isSameAs(recipe);
    }

    @Test
    void updateVisibility_bypasses_ownership_for_admins() {
        when(principal.isAdmin()).thenReturn(true);
        Recipe recipe = new Recipe();
        when(recipeService.updateVisibility("r1", Visibility.PRIVATE)).thenReturn(recipe);

        assertThat(controller.updateVisibility(principal, "r1", new VisibilityRequestDto(Visibility.PRIVATE)))
                .isSameAs(recipe);
        verify(recipeService, never()).updateOwnVisibility("alice", "r1", Visibility.PRIVATE);
    }

    @Test
    void deleteRecipe_delegates_with_owner() {
        when(principal.getUsername()).thenReturn("alice");

        controller.deleteRecipe(principal, "r1");

        verify(recipeService).delete("alice", "r1");
    }
}
