package com.shaker.controller;

import com.shaker.dto.BookmarkRequestDto;
import com.shaker.entity.bar.Bookmark;
import com.shaker.security.AppUserPrincipal;
import com.shaker.service.BookmarkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Saved-for-later public recipes someone else added. Authenticated, own data only. */
@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @GetMapping
    public List<Bookmark> getMyBookmarks(@AuthenticationPrincipal AppUserPrincipal principal) {
        return bookmarkService.findForOwner(principal.getUsername());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Bookmark addBookmark(@AuthenticationPrincipal AppUserPrincipal principal,
                                @Valid @RequestBody BookmarkRequestDto request) {
        return bookmarkService.add(principal.getUsername(), request.recipeId());
    }

    @DeleteMapping("/{recipeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBookmark(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable String recipeId) {
        bookmarkService.remove(principal.getUsername(), recipeId);
    }
}
