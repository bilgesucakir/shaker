package com.shaker.guideline;

import com.shaker.common.ApiExceptions.NotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public, anonymous-readable endpoints (see {@code SecurityConfig}).
 */
@RestController
@RequestMapping("/api/guidelines")
public class GuidelineController {

    private final GuidelineRepository guidelines;

    public GuidelineController(GuidelineRepository guidelines) {
        this.guidelines = guidelines;
    }

    @GetMapping
    public List<Guideline> list() {
        return guidelines.findAllByOrderBySortOrderAscTitleAsc();
    }

    @GetMapping("/{slug}")
    public Guideline bySlug(@PathVariable String slug) {
        return guidelines.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("No guideline '" + slug + "'"));
    }
}
