package com.shaker.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;


//will be updated
public record DiaryEntryRequestDto(
        @NotBlank @Size(max = 120) String drinkName,
        @Min(1) @Max(5) Integer rating,
        @Size(max = 2000) String notes,
        LocalDate loggedOn) {
}
