package com.shaker.repository;

import com.shaker.entity.recipe.Ingredient;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends MongoRepository<Ingredient, String> {

    List<Ingredient> findAllByOrderByNameAsc();

    Optional<Ingredient> findByName(String name);

    boolean existsByName(String name);
}
