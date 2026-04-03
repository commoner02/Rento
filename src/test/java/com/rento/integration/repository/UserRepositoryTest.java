package com.rento.integration.repository;

import com.rento.model.User;
import com.rento.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired private UserRepository userRepository;
    @Autowired private TestEntityManager em;

    @Test
    void findByEmail_ExistingEmail_ReturnsUser() {
        User saved = em.persistAndFlush(User.builder()
                .email("find@test.com")
                .password("pass")
                .firstName("Find").lastName("Me")
                .role(User.Role.BUYER).build());

        Optional<User> result = userRepository.findByEmail("find@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("find@test.com");
    }

    @Test
    void existsByEmail_ExistingEmail_ReturnsTrue() {
        em.persistAndFlush(User.builder()
                .email("exists@test.com")
                .password("pass")
                .firstName("A").lastName("B")
                .role(User.Role.BUYER).build());

        assertThat(userRepository.existsByEmail("exists@test.com")).isTrue();
        assertThat(userRepository.existsByEmail("nope@test.com")).isFalse();
    }
}