package com.ebbilogue.authservice.security.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.security.access.method.P;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.ebbilogue.authservice.security.services.UserDetailsImpl;
import com.ebbilogue.authservice.security.services.UserDetailsServiceImpl;

import java.security.Key;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;


@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    
    // 注入配置文件中的值，这里注入jwtSecret用于生成签名JWT的密钥
    @Value("${ebbilogue.authservice.jwtSecret}")
    private String jwtSecret;

    // 注入配置文件中的值，这里注入jwtExpirationMs用于设置JWT的过期时间
    @Value("${ebbilogue.authservice.jwtExpirationMs}")
    private int jwtExpirationMs;

    /**
     * 生成JWT令牌, 包含Subject, 发行时间, 过期时间, 签名算法
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
            .setSubject((userPrincipal.getUsername()))
            .setIssuedAt(new Date())
            .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
            .signWith(key(), SignatureAlgorithm.HS256)
            .compact();
    }
    
    // 从配置文件中的Base64编码的JWT密钥生成一个Key对象
    public Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // 从JWT令牌中提取用户名
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody().getSubject();
    }
    
    public boolean validateJwtToken(String authToken) {
        try { 
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true; 
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) { 
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
