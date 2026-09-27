package com.shaker.controller;

import com.shaker.dto.GuidelineRequestDto;
import com.shaker.dto.ModerationStatusRequestDto;
import com.shaker.entity.guideline.Guideline;
import com.shaker.service.GuidelineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

/** Reads are public; writes require {@code ROLE_ADMIN} (enforced in {@code SecurityConfig}). */
@RestController
@RequestMapping("/api/guidelines")
public class GuidelineController {

    private final GuidelineService guidelineService;

    public GuidelineController(GuidelineService guidelineService) {
        this.guidelineService = guidelineService;
    }

    @GetMapping
    public List<Guideline> getGuidelines() {
        return guidelineService.findAll();
    }

    @GetMapping("/{slug}")
    public Guideline getGuidelineBySlug(@PathVariable String slug) {
        return guidelineService.getGuidelineBySlug(slug);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Guideline createGuideline(@Valid @RequestBody GuidelineRequestDto request) {
        return guidelineService.create(request);
    }

    @PutMapping("/{id}")
    public Guideline updateGuideline(@PathVariable String id, @Valid @RequestBody GuidelineRequestDto request) {
        return guidelineService.update(id, request);
    }

    @PutMapping("/{id}/moderation-status")
    public Guideline updateModerationStatus(@PathVariable String id,
                                            @Valid @RequestBody ModerationStatusRequestDto request) {
        return guidelineService.updateModerationStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGuideline(@PathVariable String id) {
        guidelineService.delete(id);
    }
}
