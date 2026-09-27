package com.shaker.entity.recipe;

import lombok.Getter;
import lombok.Setter;

/** One color layer in a {@link GeneratedImageSpec}, e.g. one ingredient's contribution. */
@Getter
@Setter
public class ColorBand {

    private String colorHex;

    /** Share of the drink's volume this band represents, 0..1. */
    private double proportion;
}
