package com.shaker.dto;

import com.shaker.entity.common.Visibility;
import jakarta.validation.constraints.NotNull;

public record VisibilityRequestDto(@NotNull Visibility visibility) {
}
