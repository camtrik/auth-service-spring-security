package com.ebbilogue.authservice.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import com.ebbilogue.authservice.models.User;
import com.ebbilogue.authservice.payload.request.EditProfileRequest;
import com.ebbilogue.authservice.payload.response.JwtResponse;
import com.ebbilogue.authservice.payload.response.MessageResponse;
import com.ebbilogue.authservice.repository.UserRepository;
import com.ebbilogue.authservice.security.jwt.JwtUtils;
import com.ebbilogue.authservice.security.services.UserDetailsImpl;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserRepository userRepository; 

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@Valid @RequestBody EditProfileRequest request) {
        // 获取当前认证用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Unauthorized"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Optional<User> userOptional = userRepository.findById(userDetails.getId());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("User not found"));
        }

        if (request.getUsername() != null && !request.getUsername().equals(userDetails.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Username already exists"));
            }
        }

        if (request.getEmail() != null && !request.getEmail().equals(userDetails.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Email already exists"));
            }
        }

        User user = userOptional.get();

        if (request.getUsername() != null && !request.getUsername().isEmpty()
         && !request.getUsername().equals(userDetails.getUsername())) {
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().isEmpty()
         && !request.getEmail().equals(userDetails.getEmail())) {
            user.setEmail(request.getEmail());
        }

        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()
         && !request.getAvatarUrl().equals(userDetails.getAvatarUrl())) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        userRepository.save(user);

        // 创建一个新的 UserDetails
        UserDetailsImpl updatedUserDetails = UserDetailsImpl.build(user);

        // 创建一个新的 Authentication 对象，无需重新验证密码
        Authentication newAuthentication = new UsernamePasswordAuthenticationToken(
                updatedUserDetails, null, updatedUserDetails.getAuthorities());

        // 更新 SecurityContext
        SecurityContextHolder.getContext().setAuthentication(newAuthentication);

        List<String> roles = userDetails.getAuthorities().stream()
            .map(item -> item.getAuthority())
            .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(
                jwtUtils.generateJwtToken(newAuthentication),
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles,
                user.getAvatarUrl()
        ));
    }
}
