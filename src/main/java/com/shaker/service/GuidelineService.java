package com.shaker.service;

import com.shaker.dto.GuidelineRequestDto;
import com.shaker.entity.guideline.Guideline;
import com.shaker.entity.guideline.ModerationStatus;
import com.shaker.exception.ConflictException;
import com.shaker.exception.NotFoundException;
import com.shaker.repository.GuidelineRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Reads are public; writes require {@code ROLE_ADMIN} (enforced in {@code SecurityConfig}). */
@Service
public class GuidelineService {

    private final GuidelineRepository guidelines;

    public GuidelineService(GuidelineRepository guidelines) {
        this.guidelines = guidelines;
    }

    public List<Guideline> findAll() {
        return guidelines.findAllByOrderBySortOrderAscTitleAsc();
    }

    public Guideline getGuidelineBySlug(String slug) {
        return guidelines.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("No guideline '" + slug + "'"));
    }

    public Guideline create(GuidelineRequestDto request) {
        if (guidelines.existsBySlug(request.slug().trim())) {
            throw new ConflictException("A guideline with slug '" + request.slug().trim() + "' already exists");
        }
        Guideline guideline = new Guideline();
        applyRequest(guideline, request);
        return guidelines.save(guideline);
    }

    public Guideline update(String id, GuidelineRequestDto request) {
        Guideline guideline = getById(id);
        applyRequest(guideline, request);
        return guidelines.save(guideline);
    }

    public void delete(String id) {
        guidelines.delete(getById(id));
    }

    public Guideline updateModerationStatus(String id, ModerationStatus status) {
        Guideline guideline = getById(id);
        guideline.setModerationStatus(status);
        return guidelines.save(guideline);
    }

    private Guideline getById(String id) {
        return guidelines.findById(id).orElseThrow(() -> new NotFoundException("Guideline not found"));
    }

    private void applyRequest(Guideline guideline, GuidelineRequestDto request) {
        guideline.setSlug(request.slug().trim());
        guideline.setTitle(request.title().trim());
        guideline.setCategory(request.category());
        guideline.setBody(request.body());
        guideline.setSortOrder(request.sortOrder());
        guideline.setContentType(request.contentType());
        guideline.setAuthorType(request.authorType());
        guideline.setAuthorUsername(request.authorUsername());
        guideline.setVideoUrl(request.videoUrl());
        guideline.setRelatedRecipeId(request.relatedRecipeId());
        Set<String> spiritTags = request.relatedSpiritTags();
        guideline.setRelatedSpiritTags(spiritTags != null ? new LinkedHashSet<>(spiritTags) : Set.of());
    }
}
