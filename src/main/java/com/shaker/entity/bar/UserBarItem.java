package com.shaker.entity.bar;

import com.shaker.entity.common.UserOwnedEntity;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

/**
 * One thing a user's home bar has - either a catalogued ingredient they own, or a piece of
 * {@link Equipment} (shaker, jigger, ...). Powers "what can I make" matching and the display
 * of a user's bar.
 */
@Getter
@Setter
@Document("user_bar_items")
public class UserBarItem extends UserOwnedEntity {

    private BarItemType itemType;

    /** Set when itemType == INGREDIENT; the referenced Ingredient's id. */
    private String ingredientRef;

    /** Set when itemType == EQUIPMENT. */
    private Equipment equipment;
}
