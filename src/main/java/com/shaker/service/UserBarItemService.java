package com.shaker.service;

import com.shaker.dto.BarItemRequestDto;
import com.shaker.entity.bar.BarItemType;
import com.shaker.entity.bar.UserBarItem;
import com.shaker.exception.ConflictException;
import com.shaker.exception.NotFoundException;
import com.shaker.repository.UserBarItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserBarItemService {

    private final UserBarItemRepository barItems;

    public UserBarItemService(UserBarItemRepository barItems) {
        this.barItems = barItems;
    }

    public List<UserBarItem> findForOwner(String ownerUsername) {
        return barItems.findByOwnerUsername(ownerUsername);
    }

    public UserBarItem add(String ownerUsername, BarItemRequestDto request) {
        if (request.itemType() == BarItemType.INGREDIENT
                && barItems.existsByOwnerUsernameAndIngredientRef(ownerUsername, request.ingredientRef())) {
            throw new ConflictException("That ingredient is already in your bar");
        }

        UserBarItem item = new UserBarItem();
        item.setOwnerUsername(ownerUsername);
        item.setItemType(request.itemType());
        item.setIngredientRef(request.itemType() == BarItemType.INGREDIENT ? request.ingredientRef() : null);
        item.setEquipment(request.itemType() == BarItemType.EQUIPMENT ? request.equipment() : null);
        return barItems.save(item);
    }

    public void remove(String ownerUsername, String id) {
        UserBarItem item = barItems.findByIdAndOwnerUsername(id, ownerUsername)
                .orElseThrow(() -> new NotFoundException("Bar item not found"));
        barItems.delete(item);
    }
}
