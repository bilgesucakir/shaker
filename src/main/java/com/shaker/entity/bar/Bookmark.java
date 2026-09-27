package com.shaker.entity.bar;

import com.shaker.entity.common.UserOwnedEntity;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

/**
 * A saved-for-later public recipe someone else added - "want to try this", as opposed to a
 * diary entry which is "I already had this".
 */
@Getter
@Setter
@Document("bookmarks")
@CompoundIndex(name = "owner_recipe_unique", def = "{'ownerUsername': 1, 'recipeId': 1}", unique = true)
public class Bookmark extends UserOwnedEntity {

    private String recipeId;
}
