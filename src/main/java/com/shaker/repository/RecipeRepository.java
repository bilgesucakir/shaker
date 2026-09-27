package com.shaker.repository;

import com.shaker.entity.recipe.Recipe;
import com.shaker.entity.common.Visibility;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends MongoRepository<Recipe, String> {

    List<Recipe> findByVisibility(Visibility visibility);

    List<Recipe> findByVisibilityAndBaseSpiritTagsContaining(Visibility visibility, String baseSpiritTag);

    List<Recipe> findByCreatedBy(String createdBy);

    Optional<Recipe> findByIdAndCreatedBy(String id, String createdBy);

    List<Recipe> findByRecipeFamilyIdOrderByVersionDesc(String recipeFamilyId);
}
