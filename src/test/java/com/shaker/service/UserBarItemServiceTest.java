package com.shaker.service;

import com.shaker.dto.BarItemRequestDto;
import com.shaker.entity.bar.BarItemType;
import com.shaker.entity.bar.Equipment;
import com.shaker.entity.bar.UserBarItem;
import com.shaker.exception.ConflictException;
import com.shaker.exception.NotFoundException;
import com.shaker.repository.UserBarItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserBarItemServiceTest {

    @Mock
    UserBarItemRepository barItems;

    @InjectMocks
    UserBarItemService barItemService;

    @Test
    void add_ingredient_item_sets_only_the_ingredient_ref() {
        when(barItems.existsByOwnerUsernameAndIngredientRef("alice", "gin-id")).thenReturn(false);
        when(barItems.save(any(UserBarItem.class))).thenAnswer(inv -> inv.getArgument(0));

        UserBarItem saved = barItemService.add("alice", new BarItemRequestDto(BarItemType.INGREDIENT, "gin-id", null));

        assertThat(saved.getOwnerUsername()).isEqualTo("alice");
        assertThat(saved.getIngredientRef()).isEqualTo("gin-id");
        assertThat(saved.getEquipment()).isNull();
    }

    @Test
    void add_equipment_item_sets_only_equipment() {
        when(barItems.save(any(UserBarItem.class))).thenAnswer(inv -> inv.getArgument(0));

        UserBarItem saved = barItemService.add("alice", new BarItemRequestDto(BarItemType.EQUIPMENT, null, Equipment.JIGGER));

        assertThat(saved.getEquipment()).isEqualTo(Equipment.JIGGER);
        assertThat(saved.getIngredientRef()).isNull();
    }

    @Test
    void add_rejects_a_duplicate_ingredient() {
        when(barItems.existsByOwnerUsernameAndIngredientRef("alice", "gin-id")).thenReturn(true);

        assertThatThrownBy(() -> barItemService.add("alice", new BarItemRequestDto(BarItemType.INGREDIENT, "gin-id", null)))
                .isInstanceOf(ConflictException.class);
        verify(barItems, never()).save(any());
    }

    @Test
    void remove_deletes_an_owned_item_and_rejects_others() {
        UserBarItem item = new UserBarItem();
        when(barItems.findByIdAndOwnerUsername("item-1", "alice")).thenReturn(Optional.of(item));
        when(barItems.findByIdAndOwnerUsername("item-1", "bob")).thenReturn(Optional.empty());

        barItemService.remove("alice", "item-1");
        ArgumentCaptor<UserBarItem> captor = ArgumentCaptor.forClass(UserBarItem.class);
        verify(barItems).delete(captor.capture());
        assertThat(captor.getValue()).isSameAs(item);

        assertThatThrownBy(() -> barItemService.remove("bob", "item-1")).isInstanceOf(NotFoundException.class);
    }
}
