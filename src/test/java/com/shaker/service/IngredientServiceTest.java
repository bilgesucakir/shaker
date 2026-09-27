package com.shaker.service;

import com.shaker.dto.IngredientRequestDto;
import com.shaker.entity.recipe.IngredientCategory;
import com.shaker.entity.recipe.Ingredient;
import com.shaker.entity.recipe.Opacity;
import com.shaker.exception.ConflictException;
import com.shaker.exception.NotFoundException;
import com.shaker.repository.IngredientRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

    @Mock
    IngredientRepository ingredients;

    @InjectMocks
    IngredientService ingredientService;

    @Test
    void findAll_returns_name_ordered_catalog() {
        Ingredient gin = new Ingredient();
        when(ingredients.findAllByOrderByNameAsc()).thenReturn(List.of(gin));

        assertThat(ingredientService.findAll()).containsExactly(gin);
    }

    @Test
    void getById_returns_match_or_throws_not_found() {
        Ingredient gin = new Ingredient();
        when(ingredients.findById("gin-id")).thenReturn(Optional.of(gin));
        when(ingredients.findById("ghost")).thenReturn(Optional.empty());

        assertThat(ingredientService.getById("gin-id")).isSameAs(gin);
        assertThatThrownBy(() -> ingredientService.getById("ghost")).isInstanceOf(NotFoundException.class);
    }

    private static IngredientRequestDto request(String name) {
        return new IngredientRequestDto(name, Set.of("juniper spirit"), IngredientCategory.SPIRIT, "gin",
                40.0, "#FFFFFF", Opacity.CLEAR, null, Set.of(), 64.0);
    }

    @Test
    void create_rejects_a_duplicate_name() {
        when(ingredients.existsByName("Gin")).thenReturn(true);

        assertThatThrownBy(() -> ingredientService.create(request("Gin"))).isInstanceOf(ConflictException.class);
        verify(ingredients, never()).save(any());
    }

    @Test
    void create_saves_a_new_ingredient() {
        when(ingredients.existsByName("Gin")).thenReturn(false);
        when(ingredients.save(any(Ingredient.class))).thenAnswer(inv -> inv.getArgument(0));

        Ingredient saved = ingredientService.create(request("Gin"));

        assertThat(saved.getName()).isEqualTo("Gin");
        assertThat(saved.getCategory()).isEqualTo(IngredientCategory.SPIRIT);
        assertThat(saved.getAbvPercent()).isEqualTo(40.0);
    }

    @Test
    void update_overwrites_an_existing_ingredient() {
        Ingredient existing = new Ingredient();
        existing.setId("gin-id");
        when(ingredients.findById("gin-id")).thenReturn(Optional.of(existing));
        when(ingredients.save(any(Ingredient.class))).thenAnswer(inv -> inv.getArgument(0));

        Ingredient updated = ingredientService.update("gin-id", request("London Dry Gin"));

        assertThat(updated.getName()).isEqualTo("London Dry Gin");
    }

    @Test
    void delete_removes_the_ingredient() {
        Ingredient existing = new Ingredient();
        when(ingredients.findById("gin-id")).thenReturn(Optional.of(existing));

        ingredientService.delete("gin-id");

        verify(ingredients).delete(existing);
    }
}
