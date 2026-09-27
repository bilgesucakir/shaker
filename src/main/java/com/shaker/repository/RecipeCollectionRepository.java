package com.shaker.repository;

import com.shaker.entity.bar.RecipeCollection;
import com.shaker.entity.common.Visibility;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RecipeCollectionRepository extends MongoRepository<RecipeCollection, String> {

    List<RecipeCollection> findByOwnerUsername(String ownerUsername);

    List<RecipeCollection> findByVisibility(Visibility visibility);

    Optional<RecipeCollection> findByIdAndOwnerUsername(String id, String ownerUsername);
}
