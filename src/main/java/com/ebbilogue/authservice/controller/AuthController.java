package com.ebbilogue.authservice.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Optional;

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
import com.ebbilogue.authservice.payload.request.ResetPasswordRequest;
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

import com.ebbilogue.authservice.services.EmailService;
import com.ebbilogue.authservice.utils.VerificationCodeUtil;

import jakarta.transaction.Transactional;

import org.springframework.http.HttpStatus;

import com.ebbilogue.authservice.payload.request.ForgotPasswordRequest;


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
    private EmailService emailService; 

    @Autowired
    JwtUtils jwtUtils; 

    @Autowired
    private VerificationCodeUtil verificationCodeUtil;

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

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
            
            if (!userOptional.isEmpty()) {
                // 生成验证码
                String code = verificationCodeUtil.generateCode();
                // 保存验证码到Redis
                verificationCodeUtil.saveCode(request.getEmail(), code);
                
                // 发送验证码邮件
                emailService.sendPasswordResetEmail(request.getEmail(), code, userOptional.get().getUsername());
            }            
            return ResponseEntity.ok(new MessageResponse("Validation vode has been sent to your email"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Failed to process send validation code request"));
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("No user found for the email"));
            }
            
            // 验证验证码
            if (!verificationCodeUtil.verifyCode(request.getEmail(), request.getCode())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Invalid or expired validation code"));
            }
            
            // 更新密码
            User user = userOptional.get();
            user.setPassword(encoder.encode(request.getNewPassword()));
            userRepository.save(user);
            
            return ResponseEntity.ok(new MessageResponse("Password reset successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Failed to reset password: " + e.getMessage()));
        }
    }
}