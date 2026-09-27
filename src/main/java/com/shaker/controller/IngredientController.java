package com.shaker.controller;

import com.shaker.dto.IngredientRequestDto;
import com.shaker.entity.recipe.Ingredient;
import com.shaker.service.IngredientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Reads are public; writes require {@code ROLE_ADMIN} (enforced in {@code SecurityConfig}). */
@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @GetMapping
    public List<Ingredient> getIngredients() {
        return ingredientService.findAll();
    }

    @GetMapping("/{id}")
    public Ingredient getIngredient(@PathVariable String id) {
        return ingredientService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Ingredient createIngredient(@Valid @RequestBody IngredientRequestDto request) {
        return ingredientService.create(request);
    }

    @PutMapping("/{id}")
    public Ingredient updateIngredient(@PathVariable String id, @Valid @RequestBody IngredientRequestDto request) {
        return ingredientService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIngredient(@PathVariable String id) {
        ingredientService.delete(id);
    }
}
