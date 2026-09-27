package com.shaker.dto;

import com.shaker.entity.bar.BarItemType;
import com.shaker.entity.bar.Equipment;
import jakarta.validation.constraints.NotNull;

public record BarItemRequestDto(
        @NotNull
        BarItemType itemType,

        /** Required when itemType == INGREDIENT. */
        String ingredientRef,

        /** Required when itemType == EQUIPMENT. */
        Equipment equipment) {
}
