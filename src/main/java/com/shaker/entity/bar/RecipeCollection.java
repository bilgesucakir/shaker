package com.shaker.entity.bar;

import com.shaker.entity.common.UserOwnedEntity;
import com.shaker.entity.common.Visibility;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** A named group of recipes, e.g. "My Brunch Cocktails" - like a Letterboxd list. */
@Getter
@Setter
@Document("recipe_collections")
public class RecipeCollection extends UserOwnedEntity {

    private String name;

    private String description;

    private Visibility visibility = Visibility.PRIVATE;

    private List<String> recipeIds = new ArrayList<>();

    private Instant updatedAt = Instant.now();
}
