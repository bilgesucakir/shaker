package com.shaker.controller;

import com.shaker.dto.DiaryEntryRequestDto;
import com.shaker.entity.diary.DiaryEntry;
import com.shaker.security.AppUserPrincipal;
import com.shaker.service.DiaryService;
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

/**
 * Authenticated, per-user resource. All operations are scoped to the caller.
 */
@RestController
@RequestMapping("/api/diary")
public class DiaryController {

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    @GetMapping
    public List<DiaryEntry> getDiaries(@AuthenticationPrincipal AppUserPrincipal principal) {
        return diaryService.getDiaryEntries(principal.getUsername());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DiaryEntry createDiary(@AuthenticationPrincipal AppUserPrincipal principal,
                                  @Valid @RequestBody DiaryEntryRequestDto request) {
        return diaryService.create(principal.getUsername(), request);
    }

    @GetMapping("/{id}")
    public DiaryEntry getDiary(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable String id) {
        return diaryService.getDiaryById(id, principal.getUsername());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDiary(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable String id) {
        diaryService.delete(id, principal.getUsername());
    }
}
