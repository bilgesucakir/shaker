package com.shaker.controller;

import com.shaker.dto.BarItemRequestDto;
import com.shaker.entity.bar.UserBarItem;
import com.shaker.security.AppUserPrincipal;
import com.shaker.service.UserBarItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** A user's home bar - ingredients and equipment they own. Authenticated, own data only. */
@RestController
@RequestMapping("/api/bar")
public class BarController {

    private final UserBarItemService barItemService;

    public BarController(UserBarItemService barItemService) {
        this.barItemService = barItemService;
    }

    @GetMapping
    public List<UserBarItem> getMyBar(@AuthenticationPrincipal AppUserPrincipal principal) {
        return barItemService.findForOwner(principal.getUsername());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserBarItem addBarItem(@AuthenticationPrincipal AppUserPrincipal principal,
                                  @Valid @RequestBody BarItemRequestDto request) {
        return barItemService.add(principal.getUsername(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBarItem(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable String id) {
        barItemService.remove(principal.getUsername(), id);
    }
}
