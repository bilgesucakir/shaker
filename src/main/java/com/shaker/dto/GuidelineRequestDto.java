package com.shaker.dto;

import com.shaker.entity.guideline.GuidelineAuthorType;
import com.shaker.entity.guideline.GuidelineContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record GuidelineRequestDto(
        @NotBlank @Size(max = 80)
        String slug,

        @NotBlank @Size(max = 120)
        String title,

        @Size(max = 60)
        String category,

        @NotBlank @Size(max = 5000)
        String body,

        int sortOrder,

        @NotNull
        GuidelineContentType contentType,

        @NotNull
        GuidelineAuthorType authorType,

        String authorUsername,

        String videoUrl,

        String relatedRecipeId,

        Set<String> relatedSpiritTags) {
}
