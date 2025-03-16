package com.ebbilogue.authservice.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ebbilogue.authservice.models.ERole;
import com.ebbilogue.authservice.models.Role;
import com.ebbilogue.authservice.models.User;
import com.ebbilogue.authservice.payload.request.LoginRequest;
import com.ebbilogue.authservice.payload.request.SignupRequest;
import com.ebbilogue.authservice.payload.response.JwtResponse;
import com.ebbilogue.authservice.payload.response.MessageResponse;
import com.ebbilogue.authservice.repository.RoleRepository;
import com.ebbilogue.authservice.repository.UserRepository;
import com.ebbilogue.authservice.security.jwt.JwtUtils;
import com.ebbilogue.authservice.security.services.UserDetailsImpl;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager; 
    
    @Autowired
    UserRepository userRepository; 

    @Autowired
    RoleRepository roleRepository; 

    @Autowired
    PasswordEncoder encoder; 

    @Autowired
    JwtUtils jwtUtils; 

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) { 
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())); 
        // 认证成功后，将认证对象存储在当前线程的安全上下文中
        // 这样后续的请求处理过程可以获取到当前认证用户的信息
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 由jwtUtils生成jwt令牌
        String jwt = jwtUtils.generateJwtToken(authentication); 

        // 获取当前认证用户的对象
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal(); 

        // 获取当前认证用户的角色列表
        List<String> roles = userDetails.getAuthorities().stream()
            .map(item -> item.getAuthority())
            .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(
            jwt, 
            userDetails.getId(), 
            userDetails.getUsername(), 
            userDetails.getEmail(), 
            roles));
    }
    
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) { 
        if (userRepository.existsByUsername(signupRequest.getUsername())) { 
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) { 
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // 创建新用户
        User user = new User(signupRequest.getUsername(), 
                            signupRequest.getEmail(), 
                            encoder.encode(signupRequest.getPassword()));
        
        Set<String> strRoles = signupRequest.getRole(); 
        Set<Role> roles = new HashSet<>(); 
        
        if (strRoles == null) { 
            // 如果用户没有指定角色，则默认设置为ROLE_USER
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found.")); 
            roles.add(userRole);
        } else { 
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin": 
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found.")); 
                        roles.add(adminRole);
                        break; 
                    case "mod": 
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found.")); 
                        roles.add(modRole);
                        break; 
                    default: 
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found.")); 
                        roles.add(userRole);
                        break; 
                }
            });
        }

        user.setRoles(roles); 
        userRepository.save(user); 

        return ResponseEntity.ok(new MessageResponse("User registered successfully!")); 
    }
}