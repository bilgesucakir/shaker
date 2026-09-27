package com.shaker.controller;

import com.shaker.dto.RecipeCollectionRequestDto;
import com.shaker.entity.bar.RecipeCollection;
import com.shaker.entity.common.Visibility;
import com.shaker.security.AppUserPrincipal;
import com.shaker.service.RecipeCollectionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeCollectionControllerTest {

    @Mock
    RecipeCollectionService collectionService;

    @Mock
    AppUserPrincipal principal;

    @InjectMocks
    RecipeCollectionController controller;

    @Test
    void getPublicCollections_lists_public_only() {
        RecipeCollection collection = new RecipeCollection();
        when(collectionService.listPublic()).thenReturn(List.of(collection));

        assertThat(controller.getPublicCollections()).containsExactly(collection);
    }

    @Test
    void getMyCollections_scopes_to_the_caller() {
        when(principal.getUsername()).thenReturn("alice");
        RecipeCollection collection = new RecipeCollection();
        when(collectionService.findForOwner("alice")).thenReturn(List.of(collection));

        assertThat(controller.getMyCollections(principal)).containsExactly(collection);
    }

    @Test
    void createCollection_delegates_with_the_callers_username() {
        when(principal.getUsername()).thenReturn("alice");
        RecipeCollectionRequestDto request = new RecipeCollectionRequestDto("Brunch", null, Visibility.PUBLIC);
        RecipeCollection created = new RecipeCollection();
        when(collectionService.create("alice", request)).thenReturn(created);

        assertThat(controller.createCollection(principal, request)).isSameAs(created);
    }

    @Test
    void addRecipe_delegates_with_owner_collection_and_recipe_ids() {
        when(principal.getUsername()).thenReturn("alice");
        RecipeCollection updated = new RecipeCollection();
        when(collectionService.addRecipe("alice", "c1", "r1")).thenReturn(updated);

        assertThat(controller.addRecipe(principal, "c1", "r1")).isSameAs(updated);
    }

    @Test
    void deleteCollection_scopes_deletion_to_the_caller() {
        when(principal.getUsername()).thenReturn("alice");

        controller.deleteCollection(principal, "c1");

        verify(collectionService).delete("alice", "c1");
    }
}
