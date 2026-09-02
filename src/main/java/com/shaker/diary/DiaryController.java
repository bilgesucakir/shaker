package com.shaker.diary;

import com.shaker.common.ApiExceptions.NotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Authenticated, per-user resource. All operations are scoped to the caller.
 */
@RestController
@RequestMapping("/api/diary")
public class DiaryController {

    private final DiaryRepository entries;

    public DiaryController(DiaryRepository entries) {
        this.entries = entries;
    }

    @GetMapping
    public List<DiaryEntry> mine(Authentication authentication) {
        return entries.findByOwnerUsernameOrderByLoggedOnDescCreatedAtDesc(authentication.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DiaryEntry create(Authentication authentication, @Valid @RequestBody DiaryEntryRequest request) {
        DiaryEntry entry = new DiaryEntry();
        entry.setOwnerUsername(authentication.getName());
        apply(entry, request);
        return entries.save(entry);
    }

    @GetMapping("/{id}")
    public DiaryEntry one(Authentication authentication, @PathVariable String id) {
        return entries.findByIdAndOwnerUsername(id, authentication.getName())
                .orElseThrow(() -> new NotFoundException("Diary entry not found"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable String id) {
        DiaryEntry entry = entries.findByIdAndOwnerUsername(id, authentication.getName())
                .orElseThrow(() -> new NotFoundException("Diary entry not found"));
        entries.delete(entry);
    }

    private void apply(DiaryEntry entry, DiaryEntryRequest request) {
        entry.setDrinkName(request.drinkName().trim());
        entry.setRating(request.rating());
        entry.setNotes(request.notes());
        entry.setLoggedOn(request.loggedOn() != null ? request.loggedOn() : LocalDate.now());
    }

    public record DiaryEntryRequest(
            @NotBlank @Size(max = 120) String drinkName,
            @Min(1) @Max(5) Integer rating,
            @Size(max = 2000) String notes,
            LocalDate loggedOn) {
    }
}
