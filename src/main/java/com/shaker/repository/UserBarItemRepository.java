package com.shaker.repository;

import com.shaker.entity.bar.UserBarItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserBarItemRepository extends MongoRepository<UserBarItem, String> {

    List<UserBarItem> findByOwnerUsername(String ownerUsername);

    Optional<UserBarItem> findByIdAndOwnerUsername(String id, String ownerUsername);

    boolean existsByOwnerUsernameAndIngredientRef(String ownerUsername, String ingredientRef);
}
