package com.shaker.service;

import com.shaker.entity.bar.Bookmark;
import com.shaker.entity.recipe.Recipe;
import com.shaker.exception.ConflictException;
import com.shaker.exception.NotFoundException;
import com.shaker.repository.BookmarkRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/** "Want to try this" - for recipes someone else added, not your own. */
@Service
public class BookmarkService {

    private final BookmarkRepository bookmarks;
    private final RecipeService recipeService;

    public BookmarkService(BookmarkRepository bookmarks, RecipeService recipeService) {
        this.bookmarks = bookmarks;
        this.recipeService = recipeService;
    }

    public List<Bookmark> findForOwner(String ownerUsername) {
        return bookmarks.findByOwnerUsername(ownerUsername);
    }

    public Bookmark add(String ownerUsername, String recipeId) {
        Recipe recipe = recipeService.getVisible(recipeId, ownerUsername);
        if (ownerUsername.equals(recipe.getCreatedBy())) {
            throw new ConflictException("You can't bookmark your own recipe");
        }
        if (bookmarks.existsByOwnerUsernameAndRecipeId(ownerUsername, recipeId)) {
            throw new ConflictException("Already bookmarked");
        }

        Bookmark bookmark = new Bookmark();
        bookmark.setOwnerUsername(ownerUsername);
        bookmark.setRecipeId(recipeId);
        return bookmarks.save(bookmark);
    }

    public void remove(String ownerUsername, String recipeId) {
        Bookmark bookmark = bookmarks.findByOwnerUsernameAndRecipeId(ownerUsername, recipeId)
                .orElseThrow(() -> new NotFoundException("Bookmark not found"));
        bookmarks.delete(bookmark);
    }
}
