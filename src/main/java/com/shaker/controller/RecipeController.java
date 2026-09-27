package com.shaker.controller;

import com.shaker.dto.RecipeAvailabilityDto;
import com.shaker.dto.RecipeRequestDto;
import com.shaker.dto.VisibilityRequestDto;
import com.shaker.entity.recipe.Recipe;
import com.shaker.security.AppUserPrincipal;
import com.shaker.service.RecipeService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin capabilities live on this same resource, gated by role rather than a separate
 * {@code /api/admin/**} path: {@code ?all=true} (list) and {@code ?asClassic=true}
 * (create) only take effect for callers with {@code ROLE_ADMIN}, silently ignored otherwise.
 */
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    /**
     * Public listing - PUBLIC-visibility recipes, optionally filtered by base spirit.
     * Admins passing {@code all=true} get every recipe regardless of visibility instead.
     */
    @GetMapping
    public List<Recipe> getPublicRecipes(@AuthenticationPrincipal AppUserPrincipal principal,
                                         @RequestParam(required = false) String baseSpirit,
                                         @RequestParam(required = false) Boolean all) {
        if (Boolean.TRUE.equals(all) && principal != null && principal.isAdmin()) {
            return recipeService.findAllForAdmin();
        }
        return recipeService.listPublic(baseSpirit);
    }

    @GetMapping("/mine")
    public List<Recipe> getMyRecipes(@AuthenticationPrincipal AppUserPrincipal principal) {
        return recipeService.listMine(principal.getUsername());
    }

    /** Public if the recipe is PUBLIC; otherwise only the owner may view it (anonymous OK for public ones). */
    @GetMapping("/{id}")
    public Recipe getRecipe(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable String id) {
        return recipeService.getVisible(id, principal != null ? principal.getUsername() : null);
    }

    @GetMapping("/{id}/availability")
    public RecipeAvailabilityDto getAvailability(@AuthenticationPrincipal AppUserPrincipal principal,
                                              @PathVariable String id) {
        return recipeService.checkAvailability(id, principal.getUsername());
    }

    /**
     * Creates a new original owned by the caller. Admins passing {@code asClassic=true}
     * instead get an official, owner-less, always-public classic (e.g. Gin Tonic).
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Recipe createRecipe(@AuthenticationPrincipal AppUserPrincipal principal,
                               @RequestParam(required = false) Boolean asClassic,
                               @Valid @RequestBody RecipeRequestDto request) {
        if (Boolean.TRUE.equals(asClassic) && principal.isAdmin()) {
            return recipeService.createClassic(request);
        }
        return recipeService.create(principal.getUsername(), request);
    }

    @PostMapping("/{id}/versions")
    @ResponseStatus(HttpStatus.CREATED)
    public Recipe createVersion(@AuthenticationPrincipal AppUserPrincipal principal,
                                @PathVariable String id, @Valid @RequestBody RecipeRequestDto request) {
        return recipeService.createNewVersion(principal.getUsername(), id, request);
    }

    @PostMapping("/{id}/fork")
    @ResponseStatus(HttpStatus.CREATED)
    public Recipe forkRecipe(@AuthenticationPrincipal AppUserPrincipal principal,
                             @PathVariable String id, @Valid @RequestBody RecipeRequestDto request) {
        return recipeService.fork(principal.getUsername(), id, request);
    }

    @PutMapping("/{id}")
    public Recipe updateRecipe(@AuthenticationPrincipal AppUserPrincipal principal,
                               @PathVariable String id, @Valid @RequestBody RecipeRequestDto request) {
        return recipeService.update(principal.getUsername(), id, request);
    }

    /** Owners can set their own recipe's visibility; admins can override any recipe's. */
    @PutMapping("/{id}/visibility")
    public Recipe updateVisibility(@AuthenticationPrincipal AppUserPrincipal principal,
                                   @PathVariable String id, @Valid @RequestBody VisibilityRequestDto request) {
        if (principal.isAdmin()) {
            return recipeService.updateVisibility(id, request.visibility());
        }
        return recipeService.updateOwnVisibility(principal.getUsername(), id, request.visibility());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecipe(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable String id) {
        recipeService.delete(principal.getUsername(), id);
    }
}
