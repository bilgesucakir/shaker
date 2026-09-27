package com.shaker.entity.diary;

import com.shaker.entity.common.UserOwnedEntity;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * One logged drink in a user's diary - the core "cocktail Letterboxd" record.
 * Minimal for now; ratings, tags, recipe references, photos come later.
 */
@Getter
@Setter
@Document("diary_entries")
public class DiaryEntry extends UserOwnedEntity {

    private String drinkName;

    /** 1..5, half-stars can come later. */
    private Integer rating;

    private String notes;

    private LocalDate loggedOn;
}
