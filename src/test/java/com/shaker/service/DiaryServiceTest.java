package com.shaker.service;

import com.shaker.dto.DiaryEntryRequestDto;
import com.shaker.entity.diary.DiaryEntry;
import com.shaker.exception.NotFoundException;
import com.shaker.repository.DiaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiaryServiceTest {

    @Mock
    DiaryRepository entries;

    @InjectMocks
    DiaryService diaryService;

    @Test
    void getDiaryEntries_delegates_to_owner_scoped_query() {
        DiaryEntry entry = new DiaryEntry();
        when(entries.findByOwnerUsernameOrderByLoggedOnDescCreatedAtDesc("alice"))
                .thenReturn(List.of(entry));

        assertThat(diaryService.getDiaryEntries("alice")).containsExactly(entry);
    }

    @Test
    void create_scopes_to_owner_and_trims_drink_name() {
        when(entries.save(any(DiaryEntry.class))).thenAnswer(inv -> inv.getArgument(0));
        DiaryEntryRequestDto request =
                new DiaryEntryRequestDto("  Negroni  ", 5, "equal parts", LocalDate.of(2026, 1, 2));

        DiaryEntry saved = diaryService.create("alice", request);

        assertThat(saved.getOwnerUsername()).isEqualTo("alice");
        assertThat(saved.getDrinkName()).isEqualTo("Negroni");
        assertThat(saved.getRating()).isEqualTo(5);
        assertThat(saved.getNotes()).isEqualTo("equal parts");
        assertThat(saved.getLoggedOn()).isEqualTo(LocalDate.of(2026, 1, 2));
    }

    @Test
    void create_defaults_logged_on_to_today_when_absent() {
        when(entries.save(any(DiaryEntry.class))).thenAnswer(inv -> inv.getArgument(0));
        DiaryEntryRequestDto request = new DiaryEntryRequestDto("Old Fashioned", null, null, null);

        DiaryEntry saved = diaryService.create("alice", request);

        assertThat(saved.getLoggedOn()).isEqualTo(LocalDate.now());
        assertThat(saved.getRating()).isNull();
    }

    @Test
    void getDiaryById_returns_entry_when_owned() {
        DiaryEntry entry = new DiaryEntry();
        when(entries.findByIdAndOwnerUsername("id1", "alice")).thenReturn(Optional.of(entry));

        assertThat(diaryService.getDiaryById("id1", "alice")).isSameAs(entry);
    }

    @Test
    void getDiaryById_throws_when_missing_or_owned_by_someone_else() {
        when(entries.findByIdAndOwnerUsername("id1", "bob")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diaryService.getDiaryById("id1", "bob"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_removes_the_owned_entry() {
        DiaryEntry entry = new DiaryEntry();
        when(entries.findByIdAndOwnerUsername("id1", "alice")).thenReturn(Optional.of(entry));

        diaryService.delete("id1", "alice");

        verify(entries).delete(entry);
    }

    @Test
    void delete_throws_and_does_not_delete_when_entry_missing() {
        when(entries.findByIdAndOwnerUsername("id1", "alice")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diaryService.delete("id1", "alice"))
                .isInstanceOf(NotFoundException.class);
        verify(entries, never()).delete(any(DiaryEntry.class));
    }
}
