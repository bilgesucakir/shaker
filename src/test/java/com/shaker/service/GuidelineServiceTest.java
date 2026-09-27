package com.shaker.service;

import com.shaker.dto.GuidelineRequestDto;
import com.shaker.entity.guideline.Guideline;
import com.shaker.entity.guideline.GuidelineAuthorType;
import com.shaker.entity.guideline.GuidelineContentType;
import com.shaker.entity.guideline.ModerationStatus;
import com.shaker.exception.ConflictException;
import com.shaker.exception.NotFoundException;
import com.shaker.repository.GuidelineRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuidelineServiceTest {

    @Mock
    GuidelineRepository guidelines;

    @InjectMocks
    GuidelineService guidelineService;

    private static GuidelineRequestDto request(String slug) {
        return new GuidelineRequestDto(slug, "Bar Basics", "Fundamentals", "Chill your glassware.", 1,
                GuidelineContentType.ARTICLE, GuidelineAuthorType.EDITORIAL, null, null, null, Set.of("gin"));
    }

    @Test
    void findAll_returns_repository_ordered_list() {
        Guideline guideline = new Guideline();
        when(guidelines.findAllByOrderBySortOrderAscTitleAsc()).thenReturn(List.of(guideline));

        assertThat(guidelineService.findAll()).containsExactly(guideline);
    }

    @Test
    void getGuidelineBySlug_returns_match() {
        Guideline guideline = new Guideline();
        when(guidelines.findBySlug("bar-basics")).thenReturn(Optional.of(guideline));

        assertThat(guidelineService.getGuidelineBySlug("bar-basics")).isSameAs(guideline);
    }

    @Test
    void getGuidelineBySlug_throws_not_found_naming_the_slug() {
        when(guidelines.findBySlug("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> guidelineService.getGuidelineBySlug("nope"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("nope");
    }

    @Test
    void create_rejects_a_duplicate_slug() {
        when(guidelines.existsBySlug("bar-basics")).thenReturn(true);

        assertThatThrownBy(() -> guidelineService.create(request("bar-basics")))
                .isInstanceOf(ConflictException.class);
        verify(guidelines, never()).save(any());
    }

    @Test
    void create_saves_a_new_guideline() {
        when(guidelines.existsBySlug("bar-basics")).thenReturn(false);
        when(guidelines.save(any(Guideline.class))).thenAnswer(inv -> inv.getArgument(0));

        Guideline saved = guidelineService.create(request("bar-basics"));

        assertThat(saved.getSlug()).isEqualTo("bar-basics");
        assertThat(saved.getRelatedSpiritTags()).containsExactly("gin");
    }

    @Test
    void updateModerationStatus_changes_only_the_status() {
        Guideline guideline = new Guideline();
        guideline.setModerationStatus(ModerationStatus.PENDING);
        when(guidelines.findById("g1")).thenReturn(Optional.of(guideline));
        when(guidelines.save(any(Guideline.class))).thenAnswer(inv -> inv.getArgument(0));

        Guideline updated = guidelineService.updateModerationStatus("g1", ModerationStatus.APPROVED);

        assertThat(updated.getModerationStatus()).isEqualTo(ModerationStatus.APPROVED);
    }

    @Test
    void delete_removes_the_guideline() {
        Guideline guideline = new Guideline();
        when(guidelines.findById("g1")).thenReturn(Optional.of(guideline));

        guidelineService.delete("g1");

        verify(guidelines).delete(guideline);
    }
}
