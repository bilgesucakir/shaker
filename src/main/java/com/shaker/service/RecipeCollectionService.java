package com.shaker.service;

import com.shaker.dto.RecipeCollectionRequestDto;
import com.shaker.entity.bar.RecipeCollection;
import com.shaker.entity.common.Visibility;
import com.shaker.exception.NotFoundException;
import com.shaker.repository.RecipeCollectionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class RecipeCollectionService {

    private final RecipeCollectionRepository collections;
    private final RecipeService recipeService;

    public RecipeCollectionService(RecipeCollectionRepository collections, RecipeService recipeService) {
        this.collections = collections;
        this.recipeService = recipeService;
    }

    public List<RecipeCollection> listPublic() {
        return collections.findByVisibility(Visibility.PUBLIC);
    }

    public List<RecipeCollection> findForOwner(String ownerUsername) {
        return collections.findByOwnerUsername(ownerUsername);
    }

    public RecipeCollection create(String ownerUsername, RecipeCollectionRequestDto request) {
        RecipeCollection collection = new RecipeCollection();
        collection.setOwnerUsername(ownerUsername);
        applyRequest(collection, request);
        return collections.save(collection);
    }

    public RecipeCollection update(String ownerUsername, String id, RecipeCollectionRequestDto request) {
        RecipeCollection collection = getOwned(id, ownerUsername);
        applyRequest(collection, request);
        return collections.save(collection);
    }

    public RecipeCollection addRecipe(String ownerUsername, String collectionId, String recipeId) {
        RecipeCollection collection = getOwned(collectionId, ownerUsername);
        recipeService.getVisible(recipeId, ownerUsername); // 404s if the recipe isn't visible to them
        if (!collection.getRecipeIds().contains(recipeId)) {
            collection.getRecipeIds().add(recipeId);
            collection.setUpdatedAt(Instant.now());
            collections.save(collection);
        }
        return collection;
    }

    public RecipeCollection removeRecipe(String ownerUsername, String collectionId, String recipeId) {
        RecipeCollection collection = getOwned(collectionId, ownerUsername);
        collection.getRecipeIds().remove(recipeId);
        collection.setUpdatedAt(Instant.now());
        return collections.save(collection);
    }

    public void delete(String ownerUsername, String id) {
        collections.delete(getOwned(id, ownerUsername));
    }

    private RecipeCollection getOwned(String id, String ownerUsername) {
        return collections.findByIdAndOwnerUsername(id, ownerUsername)
                .orElseThrow(() -> new NotFoundException("Collection not found"));
    }

    private void applyRequest(RecipeCollection collection, RecipeCollectionRequestDto request) {
        collection.setName(request.name().trim());
        collection.setDescription(request.description());
        collection.setVisibility(request.visibility());
        collection.setUpdatedAt(Instant.now());
    }
}
