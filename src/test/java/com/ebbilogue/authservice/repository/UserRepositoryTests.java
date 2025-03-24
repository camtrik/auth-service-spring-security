package com.ebbilogue.authservice.repository;

import com.ebbilogue.authservice.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void shouldFindUserByUsername() {
        // 创建测试用户
        User user = new User("testuser111", "test111@test.com", "password123");
        entityManager.persist(user);
        entityManager.flush();

        // 测试查找功能
        User found = userRepository.findByUsername("testuser111").orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    public void shouldCheckUsernameExists() {
        // 创建测试用户
        User user = new User("testuser111", "test111@test.com", "password123");
        entityManager.persist(user);
        entityManager.flush();

        // 测试用户名存在检查
        boolean exists = userRepository.existsByUsername("testuser111");
        assertThat(exists).isTrue();
    }
}