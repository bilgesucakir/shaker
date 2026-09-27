package com.shaker.controller;

import com.shaker.entity.recipe.Ingredient;
import com.shaker.service.IngredientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngredientControllerTest {

    @Mock
    IngredientService ingredientService;

    @InjectMocks
    IngredientController controller;

    @Test
    void getIngredients_delegates_to_the_service() {
        Ingredient gin = new Ingredient();
        when(ingredientService.findAll()).thenReturn(List.of(gin));

        assertThat(controller.getIngredients()).containsExactly(gin);
    }

    @Test
    void getIngredient_delegates_by_id() {
        Ingredient gin = new Ingredient();
        when(ingredientService.getById("gin-id")).thenReturn(gin);

        assertThat(controller.getIngredient("gin-id")).isSameAs(gin);
    }
}
