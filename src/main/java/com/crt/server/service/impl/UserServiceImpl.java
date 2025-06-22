package com.crt.server.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crt.server.dto.AuthResponseDTO;
import com.crt.server.dto.UserDTO;
import com.crt.server.exception.ResourceNotFoundException;
import com.crt.server.model.User;
import com.crt.server.repository.UserRepository;
import com.crt.server.service.EmailService;
import com.crt.server.service.UserService;
import com.crt.server.util.PasswordGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public UserDTO createUser(UserDTO createUserDTO) {
        // Check if username or email already exists
        if (existsByUsername(createUserDTO.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (existsByEmail(createUserDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        String generatedPassword = PasswordGenerator.generatePassword();

        User user = User.builder()
                .name(createUserDTO.getName())
                .email(createUserDTO.getEmail())
                .phone(createUserDTO.getPhone())
                .username(createUserDTO.getUsername())
                .password(passwordEncoder.encode(generatedPassword))
                .role(createUserDTO.getRole())
                .isFirstLogin(true)
                .build();

        // Save user
        User savedUser = userRepository.save(user);

        // Send email with credentials
        emailService.sendPasswordEmail(savedUser.getEmail(), savedUser.getUsername(), generatedPassword);

        return convertToDTO(savedUser);
    }

    @Override
    public UserDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertToDTO(user);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertToDTO(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO updateUser(UUID id, UserDTO updateUserDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update user fields
        user.setName(updateUserDTO.getName());
        user.setEmail(updateUserDTO.getEmail());
        user.setPhone(updateUserDTO.getPhone());
        user.setRole(updateUserDTO.getRole());

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Override
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate new password
        String newPassword = PasswordGenerator.generatePassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Send email with new password
        emailService.sendPasswordEmail(user.getEmail(), user.getUsername(), newPassword);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public AuthResponseDTO updatePassword(UUID id, String currentPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Update the password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return AuthResponseDTO.builder()
                .message("Password updated successfully")
                .build();
    }

    @Override
    public AuthResponseDTO updatePasswordByEmail(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update the password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return AuthResponseDTO.builder()
                .message("Password updated successfully")
                .build();
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .username(user.getUsername())
                .role(user.getRole())
                .isFirstLogin(user.isFirstLogin())
                .build();
    }
}