package com.ebbilogue.authservice.security.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ebbilogue.authservice.models.User;
import com.ebbilogue.authservice.repository.UserRepository;

import jakarta.transaction.Transactional;

/**
 * 实现UserDetailsService接口，用于从数据库中查找用户信息
 */
@Service 
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository; 

    @Override 
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        
        return UserDetailsImpl.build(user);
    }
}