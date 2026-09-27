package com.shaker.controller;

import com.shaker.dto.RecipeCollectionRequestDto;
import com.shaker.entity.bar.RecipeCollection;
import com.shaker.security.AppUserPrincipal;
import com.shaker.service.RecipeCollectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

/** Named groups of recipes, e.g. "My Brunch Cocktails" - like a Letterboxd list. */
@RestController
@RequestMapping("/api/collections")
public class RecipeCollectionController {

    private final RecipeCollectionService collectionService;

    public RecipeCollectionController(RecipeCollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping
    public List<RecipeCollection> getPublicCollections() {
        return collectionService.listPublic();
    }

    @GetMapping("/mine")
    public List<RecipeCollection> getMyCollections(@AuthenticationPrincipal AppUserPrincipal principal) {
        return collectionService.findForOwner(principal.getUsername());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecipeCollection createCollection(@AuthenticationPrincipal AppUserPrincipal principal,
                                             @Valid @RequestBody RecipeCollectionRequestDto request) {
        return collectionService.create(principal.getUsername(), request);
    }

    @PutMapping("/{id}")
    public RecipeCollection updateCollection(@AuthenticationPrincipal AppUserPrincipal principal,
                                             @PathVariable String id, @Valid @RequestBody RecipeCollectionRequestDto request) {
        return collectionService.update(principal.getUsername(), id, request);
    }

    @PostMapping("/{id}/recipes/{recipeId}")
    public RecipeCollection addRecipe(@AuthenticationPrincipal AppUserPrincipal principal,
                                      @PathVariable String id, @PathVariable String recipeId) {
        return collectionService.addRecipe(principal.getUsername(), id, recipeId);
    }

    @DeleteMapping("/{id}/recipes/{recipeId}")
    public RecipeCollection removeRecipe(@AuthenticationPrincipal AppUserPrincipal principal,
                                         @PathVariable String id, @PathVariable String recipeId) {
        return collectionService.removeRecipe(principal.getUsername(), id, recipeId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCollection(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable String id) {
        collectionService.delete(principal.getUsername(), id);
    }
}
