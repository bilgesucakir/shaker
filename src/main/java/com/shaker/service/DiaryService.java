package com.shaker.service;

import com.shaker.dto.DiaryEntryRequestDto;
import com.shaker.entity.diary.DiaryEntry;
import com.shaker.exception.NotFoundException;
import com.shaker.repository.DiaryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DiaryService {

    private final DiaryRepository entries;

    public DiaryService(DiaryRepository entries) {
        this.entries = entries;
    }

    public List<DiaryEntry> getDiaryEntries(String ownerUsername) {
        return entries.findByOwnerUsernameOrderByLoggedOnDescCreatedAtDesc(ownerUsername);
    }

    public DiaryEntry create(String ownerUsername, DiaryEntryRequestDto request) {
        DiaryEntry entry = new DiaryEntry();
        entry.setOwnerUsername(ownerUsername);
        entry.setDrinkName(request.drinkName().trim());
        entry.setRating(request.rating());
        entry.setNotes(request.notes());
        entry.setLoggedOn(request.loggedOn() != null ? request.loggedOn() : LocalDate.now());
        return entries.save(entry);
    }

    public DiaryEntry getDiaryById(String id, String ownerUsername) {
        return entries.findByIdAndOwnerUsername(id, ownerUsername)
                .orElseThrow(() -> new NotFoundException("Diary entry not found"));
    }

    public void delete(String id, String ownerUsername) {
        entries.delete(getDiaryById(id, ownerUsername));
    }
}
