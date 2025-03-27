package com.ebbilogue.authservice.security.services;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.ebbilogue.authservice.models.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 实现UserDetails接口，封装用户详细信息
 */
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L; 

    private Long id; 
    private String username; 
    private String email; 
    private String avatarUrl;
    
    @JsonIgnore
    private String password; 

    private Collection<? extends GrantedAuthority> authorities; 

    public UserDetailsImpl(Long id, String username, String email, String password, String avatarUrl,
            Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.avatarUrl = avatarUrl;
        this.authorities = authorities;
    }

    /**
     * 将User转化为UserDetailsImpl
     * Sprint Security中，需要实现UserDetails接口，并实现build方法
     */
    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority(role.getName().name()))
            .collect(Collectors.toList());
        
        return new UserDetailsImpl(
            user.getId(), 
            user.getUsername(), 
            user.getEmail(), 
            user.getPassword(), 
            user.getAvatarUrl(),
            authorities);
    }

    @Override 
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public Long getId() {
        return id; 
    }

    public String getEmail() {
        return email; 
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; 
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; 
    }

    @Override
    public boolean isEnabled() {
        return true; 
    }

    @Override
    public boolean equals(Object o) {
        // 同一个对象（内存地址的引用相同）
        if (this == o) {
            return true; 
        }
        // 对象为空或者类型不匹配
        if (o == null || getClass() != o.getClass()) {
            return false; 
        }
        // 比较两个对象的id
        UserDetailsImpl user = (UserDetailsImpl) o; 
        return Objects.equals(id, user.id); 
    }
}
