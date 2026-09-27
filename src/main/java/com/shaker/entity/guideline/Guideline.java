package com.shaker.entity.guideline;

import com.shaker.entity.common.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Reference content shown to everyone: articles, recipe videos, pro tips, and tips other
 * users choose to share. Readable anonymously; community content can be filtered per-viewer
 * via {@code User.preferences.showCommunityTips}.
 */
@Getter
@Setter
@Document("guidelines")
public class Guideline extends BaseEntity {

    @Indexed(unique = true)
    private String slug;

    private String title;

    private String category;

    private String body;

    private int sortOrder;

    private GuidelineContentType contentType = GuidelineContentType.ARTICLE;

    private GuidelineAuthorType authorType = GuidelineAuthorType.EDITORIAL;

    /** Null for editorial content. */
    private String authorUsername;

    /** Set when contentType == VIDEO. */
    private String videoUrl;

    /** Optional - a tip attached to a specific cocktail, e.g. "pro tip for Negroni". */
    private String relatedRecipeId;

    /** Same vocabulary as {@code Recipe.baseSpiritTags}, so spirit filters cover guides too. */
    private Set<String> relatedSpiritTags = new LinkedHashSet<>();

    private ModerationStatus moderationStatus = ModerationStatus.APPROVED;

    private int helpfulCount;
}
