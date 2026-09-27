package com.shaker.dto;

import com.shaker.entity.user.VolumeUnit;
import jakarta.validation.constraints.Size;

public record UpdateAccountRequestDto(
        @Size(max = 60) String displayName,
        @Size(max = 500) String bio,
        String favoriteSpirit,
        Boolean showCommunityTips,
        VolumeUnit preferredVolumeUnit) {
}
