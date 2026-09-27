package com.shaker.entity.recipe;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything needed to draw a schematic illustration of a recipe - glass shape filled with
 * color band(s) derived from its ingredients, plus ice/garnish/carbonation overlays. Not a
 * photo and not AI-generated; a renderer (SVG or similar) builds the image from this spec.
 */
@Getter
@Setter
public class GeneratedImageSpec {

    private Glass glass;

    /** 0..1 - how full the glass is drawn, from total ingredient volume vs. the glass's typical capacity. */
    private double fillLevel;

    /**
     * Ordered color layers. For LAYERED drinks these stay distinct (ordered by ingredient
     * density); for shaken/stirred/built drinks this is usually a single blended band.
     */
    private List<ColorBand> colorBands = new ArrayList<>();

    private IceStyle iceOverlay;

    private GarnishType garnishIcon;

    private boolean carbonationOverlay;

    /** Cached rendered asset, once generated, so it isn't recomputed on every view. */
    private String renderedAssetUrl;
}
