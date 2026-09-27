package com.shaker.controller;

import com.shaker.dto.BookmarkRequestDto;
import com.shaker.entity.bar.Bookmark;
import com.shaker.security.AppUserPrincipal;
import com.shaker.service.BookmarkService;
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
class BookmarkControllerTest {

    @Mock
    BookmarkService bookmarkService;

    @Mock
    AppUserPrincipal principal;

    @InjectMocks
    BookmarkController controller;

    @Test
    void getMyBookmarks_scopes_to_the_caller() {
        when(principal.getUsername()).thenReturn("alice");
        Bookmark bookmark = new Bookmark();
        when(bookmarkService.findForOwner("alice")).thenReturn(List.of(bookmark));

        assertThat(controller.getMyBookmarks(principal)).containsExactly(bookmark);
    }

    @Test
    void addBookmark_delegates_with_the_callers_username_and_recipe_id() {
        when(principal.getUsername()).thenReturn("alice");
        Bookmark saved = new Bookmark();
        when(bookmarkService.add("alice", "r1")).thenReturn(saved);

        assertThat(controller.addBookmark(principal, new BookmarkRequestDto("r1"))).isSameAs(saved);
    }

    @Test
    void removeBookmark_scopes_removal_to_the_caller() {
        when(principal.getUsername()).thenReturn("alice");

        controller.removeBookmark(principal, "r1");

        verify(bookmarkService).remove("alice", "r1");
    }
}
