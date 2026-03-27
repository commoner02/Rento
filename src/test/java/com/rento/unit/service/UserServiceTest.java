package com.rento.unit.service;

import com.rento.dto.request.RegisterRequest;
import com.rento.exception.ResourceNotFoundException;
import com.rento.model.User;
import com.rento.repository.UserRepository;
import com.rento.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;

    private RegisterRequest validRequest;
    private User existingUser;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest();
        validRequest.setEmail("test@example.com");
        validRequest.setPassword("password123");
        validRequest.setFirstName("Test");
        validRequest.setLastName("User");
        validRequest.setPhone("01700000000");

        existingUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded")
                .firstName("Test")
                .lastName("User")
                .role(User.Role.BUYER)
                .build();
    }

    @Test
    void register_ValidData_SavesAndReturnsUser() {
        when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(validRequest.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userService.register(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getRole()).isEqualTo(User.Role.BUYER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(validRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Email already in use");

        verify(userRepository, never()).save(any());
    }

    @Test
    void findByEmail_ExistingEmail_ReturnsUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

        User result = userService.findByEmail("test@example.com");

        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void updateRole_ExistingUser_ChangesRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any())).thenReturn(existingUser);

        User result = userService.updateRole(1L, User.Role.SELLER);

        assertThat(result.getRole()).isEqualTo(User.Role.SELLER);
        verify(userRepository).save(existingUser);
    }
}