package com.shaker.service;

import com.shaker.dto.IngredientRequestDto;
import com.shaker.entity.recipe.Ingredient;
import com.shaker.exception.ConflictException;
import com.shaker.exception.NotFoundException;
import com.shaker.repository.IngredientRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Reads are public; writes are admin-only (enforced in {@code SecurityConfig}). */
@Service
public class IngredientService {

    private final IngredientRepository ingredients;

    public IngredientService(IngredientRepository ingredients) {
        this.ingredients = ingredients;
    }

    public List<Ingredient> findAll() {
        return ingredients.findAllByOrderByNameAsc();
    }

    public Ingredient getById(String id) {
        return ingredients.findById(id).orElseThrow(() -> new NotFoundException("Ingredient not found"));
    }

    public Ingredient create(IngredientRequestDto request) {
        if (ingredients.existsByName(request.name().trim())) {
            throw new ConflictException("An ingredient named '" + request.name().trim() + "' already exists");
        }
        Ingredient ingredient = new Ingredient();
        applyRequest(ingredient, request);
        return ingredients.save(ingredient);
    }

    public Ingredient update(String id, IngredientRequestDto request) {
        Ingredient ingredient = getById(id);
        applyRequest(ingredient, request);
        return ingredients.save(ingredient);
    }

    public void delete(String id) {
        ingredients.delete(getById(id));
    }

    private void applyRequest(Ingredient ingredient, IngredientRequestDto request) {
        ingredient.setName(request.name().trim());
        ingredient.setAliases(request.aliases() != null ? new LinkedHashSet<>(request.aliases()) : Set.of());
        ingredient.setCategory(request.category());
        ingredient.setSubCategory(request.subCategory());
        ingredient.setAbvPercent(request.abvPercent());
        ingredient.setColorHex(request.colorHex());
        ingredient.setOpacity(request.opacity());
        ingredient.setRelativeDensity(request.relativeDensity());
        ingredient.setAllergenTags(request.allergenTags() != null ? new LinkedHashSet<>(request.allergenTags()) : Set.of());
        ingredient.setCaloriesPerOz(request.caloriesPerOz());
    }
}
