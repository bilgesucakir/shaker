package com.shaker.controller;

import com.shaker.dto.BarItemRequestDto;
import com.shaker.entity.bar.BarItemType;
import com.shaker.entity.bar.UserBarItem;
import com.shaker.security.AppUserPrincipal;
import com.shaker.service.UserBarItemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BarControllerTest {

    @Mock
    UserBarItemService barItemService;

    @Mock
    AppUserPrincipal principal;

    @InjectMocks
    BarController controller;

    @Test
    void getMyBar_scopes_to_the_caller() {
        when(principal.getUsername()).thenReturn("alice");
        UserBarItem item = new UserBarItem();
        when(barItemService.findForOwner("alice")).thenReturn(List.of(item));

        assertThat(controller.getMyBar(principal)).containsExactly(item);
    }

    @Test
    void addBarItem_delegates_with_the_callers_username() {
        when(principal.getUsername()).thenReturn("alice");
        BarItemRequestDto request = new BarItemRequestDto(BarItemType.INGREDIENT, "gin-id", null);
        UserBarItem saved = new UserBarItem();
        when(barItemService.add("alice", request)).thenReturn(saved);

        assertThat(controller.addBarItem(principal, request)).isSameAs(saved);
    }

    @Test
    void removeBarItem_scopes_deletion_to_the_caller() {
        when(principal.getUsername()).thenReturn("alice");

        controller.removeBarItem(principal, "item-1");

        verify(barItemService).remove("alice", "item-1");
    }
}
