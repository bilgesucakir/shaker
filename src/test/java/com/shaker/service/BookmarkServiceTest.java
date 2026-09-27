package com.shaker.service;

import com.shaker.entity.bar.Bookmark;
import com.shaker.entity.common.Visibility;
import com.shaker.entity.recipe.Recipe;
import com.shaker.exception.ConflictException;
import com.shaker.exception.NotFoundException;
import com.shaker.repository.BookmarkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @Mock
    BookmarkRepository bookmarks;

    @Mock
    RecipeService recipeService;

    @InjectMocks
    BookmarkService bookmarkService;

    private static Recipe publicRecipe(String createdBy) {
        Recipe recipe = new Recipe();
        recipe.setId("r1");
        recipe.setCreatedBy(createdBy);
        recipe.setVisibility(Visibility.PUBLIC);
        return recipe;
    }

    @Test
    void add_bookmarks_someone_elses_visible_recipe() {
        when(recipeService.getVisible("r1", "alice")).thenReturn(publicRecipe("bob"));
        when(bookmarks.existsByOwnerUsernameAndRecipeId("alice", "r1")).thenReturn(false);
        when(bookmarks.save(any(Bookmark.class))).thenAnswer(inv -> inv.getArgument(0));

        Bookmark saved = bookmarkService.add("alice", "r1");

        assertThat(saved.getOwnerUsername()).isEqualTo("alice");
        assertThat(saved.getRecipeId()).isEqualTo("r1");
    }

    @Test
    void add_rejects_bookmarking_your_own_recipe() {
        when(recipeService.getVisible("r1", "alice")).thenReturn(publicRecipe("alice"));

        assertThatThrownBy(() -> bookmarkService.add("alice", "r1")).isInstanceOf(ConflictException.class);
    }

    @Test
    void add_rejects_a_duplicate_bookmark() {
        when(recipeService.getVisible("r1", "alice")).thenReturn(publicRecipe("bob"));
        when(bookmarks.existsByOwnerUsernameAndRecipeId("alice", "r1")).thenReturn(true);

        assertThatThrownBy(() -> bookmarkService.add("alice", "r1")).isInstanceOf(ConflictException.class);
    }

    @Test
    void remove_throws_when_bookmark_not_found() {
        when(bookmarks.findByOwnerUsernameAndRecipeId("alice", "r1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookmarkService.remove("alice", "r1")).isInstanceOf(NotFoundException.class);
    }
}
