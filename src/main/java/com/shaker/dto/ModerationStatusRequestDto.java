package com.shaker.dto;

import com.shaker.entity.guideline.ModerationStatus;
import jakarta.validation.constraints.NotNull;

public record ModerationStatusRequestDto(@NotNull ModerationStatus status) {
}
