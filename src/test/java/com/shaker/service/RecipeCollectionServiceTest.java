package com.shaker.service;

import com.shaker.dto.RecipeCollectionRequestDto;
import com.shaker.entity.bar.RecipeCollection;
import com.shaker.entity.common.Visibility;
import com.shaker.entity.recipe.Recipe;
import com.shaker.exception.NotFoundException;
import com.shaker.repository.RecipeCollectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeCollectionServiceTest {

    @Mock
    RecipeCollectionRepository collections;

    @Mock
    RecipeService recipeService;

    @InjectMocks
    RecipeCollectionService collectionService;

    @Test
    void create_sets_the_owner() {
        when(collections.save(any(RecipeCollection.class))).thenAnswer(inv -> inv.getArgument(0));

        RecipeCollection created = collectionService.create("alice",
                new RecipeCollectionRequestDto("Brunch", "light ones", Visibility.PUBLIC));

        assertThat(created.getOwnerUsername()).isEqualTo("alice");
        assertThat(created.getName()).isEqualTo("Brunch");
    }

    @Test
    void addRecipe_requires_the_recipe_to_be_visible_and_avoids_duplicates() {
        RecipeCollection collection = new RecipeCollection();
        collection.setOwnerUsername("alice");
        when(collections.findByIdAndOwnerUsername("c1", "alice")).thenReturn(Optional.of(collection));
        when(recipeService.getVisible("r1", "alice")).thenReturn(new Recipe());
        when(collections.save(any(RecipeCollection.class))).thenAnswer(inv -> inv.getArgument(0));

        collectionService.addRecipe("alice", "c1", "r1");
        collectionService.addRecipe("alice", "c1", "r1"); // second call should not duplicate

        assertThat(collection.getRecipeIds()).containsExactly("r1");
    }

    @Test
    void addRecipe_rejects_when_recipe_is_not_visible_to_the_caller() {
        RecipeCollection collection = new RecipeCollection();
        collection.setOwnerUsername("alice");
        when(collections.findByIdAndOwnerUsername("c1", "alice")).thenReturn(Optional.of(collection));
        when(recipeService.getVisible("r1", "alice")).thenThrow(new NotFoundException("Recipe not found"));

        assertThatThrownBy(() -> collectionService.addRecipe("alice", "c1", "r1"))
                .isInstanceOf(NotFoundException.class);
        assertThat(collection.getRecipeIds()).isEmpty();
    }

    @Test
    void removeRecipe_drops_the_id() {
        RecipeCollection collection = new RecipeCollection();
        collection.getRecipeIds().add("r1");
        when(collections.findByIdAndOwnerUsername("c1", "alice")).thenReturn(Optional.of(collection));
        when(collections.save(any(RecipeCollection.class))).thenAnswer(inv -> inv.getArgument(0));

        collectionService.removeRecipe("alice", "c1", "r1");

        assertThat(collection.getRecipeIds()).isEmpty();
    }

    @Test
    void operations_on_someone_elses_collection_404() {
        when(collections.findByIdAndOwnerUsername("c1", "mallory")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionService.delete("mallory", "c1")).isInstanceOf(NotFoundException.class);
    }
}
