package com.shaker.repository;

import com.shaker.entity.bar.Bookmark;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends MongoRepository<Bookmark, String> {

    List<Bookmark> findByOwnerUsername(String ownerUsername);

    Optional<Bookmark> findByOwnerUsernameAndRecipeId(String ownerUsername, String recipeId);

    boolean existsByOwnerUsernameAndRecipeId(String ownerUsername, String recipeId);
}
