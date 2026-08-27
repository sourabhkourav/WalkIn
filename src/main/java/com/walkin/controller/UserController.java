package com.walkin.controller;

import com.walkin.config.PageRequestFactory;
import com.walkin.dto.*;
import com.walkin.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserManagementService users;
    private final PageRequestFactory pages;

    public UserController(UserManagementService users, PageRequestFactory pages) {
        this.users = users;
        this.pages = pages;
    }

    @PostMapping
    ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(users.create(request));
    }

    @GetMapping
    PageResponse<UserResponse> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "username") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        return users.getAll(pages.create(page, size, sort, direction,
                Set.of("userId", "username", "role", "enabled")));
    }

    @PutMapping("/{id}")
    UserResponse update(@PathVariable Integer id, @Valid @RequestBody UpdateUserRequest request) {
        return users.update(id, request);
    }

    @PutMapping("/{id}/password")
    ResponseEntity<Void> resetPassword(
            @PathVariable Integer id, @Valid @RequestBody ResetPasswordRequest request) {
        users.resetPassword(id, request);
        return ResponseEntity.noContent().build();
    }
}
