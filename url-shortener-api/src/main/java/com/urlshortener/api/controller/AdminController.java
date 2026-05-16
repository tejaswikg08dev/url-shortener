package com.urlshortener.api.controller;

import com.urlshortener.api.model.Role;
import com.urlshortener.api.model.User;
import com.urlshortener.api.repository.UserRepository;
import com.urlshortener.api.service.UrlService;
import com.urlshortener.common.dto.PagedResponse;
import com.urlshortener.common.dto.UpdateRoleRequest;
import com.urlshortener.common.dto.UrlResponse;
import com.urlshortener.common.dto.UserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UrlService urlService;
    private final UserRepository userRepository;

    @GetMapping("/urls")
    public ResponseEntity<PagedResponse<UrlResponse>> getAllUrls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size){
        return ResponseEntity.ok(urlService.getAllUrls(page, size));
    }

    @DeleteMapping("/urls/{shortKey}")
    public ResponseEntity<Void> deleteUrl(@PathVariable String shortKey){
        urlService.adminDeleteUrl(shortKey);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users")
    public ResponseEntity<PagedResponse<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size){
        Page<User> users = userRepository.findAll(PageRequest.of(page, size));

        List<UserDto> content = users.getContent().stream()
                .map(u -> new UserDto(
                        u.getId(),
                        u.getEmail(),
                        u.getName(),
                        u.getRole().name(),
                        u.getCreatedAt()))
                .toList();

        return ResponseEntity.ok(new PagedResponse<>(
                content, page, size,
                users.getTotalElements(),
                users.getTotalPages(),
                users.isLast()));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserDto> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request){
        User user = userRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("User not found: "+id));

        user.setRole(Role.valueOf(request.role()));

        userRepository.save(user);

        return ResponseEntity.ok(new UserDto(
                user.getId(), user.getEmail(),
                user.getName(), user.getRole().name(),
                user.getCreatedAt()));
    }
}
