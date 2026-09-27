package com.shaker.entity.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPreferences {

    /** One of the same base-spirit tag values used in {@code Recipe.baseSpiritTags}. */
    private String favoriteSpirit;

    /** Whether community-submitted guideline tips are shown to this user. */
    private boolean showCommunityTips = true;

    private VolumeUnit preferredVolumeUnit = VolumeUnit.OZ;
}
