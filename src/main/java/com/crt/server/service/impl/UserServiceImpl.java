package com.crt.server.service.impl;

import com.crt.server.dto.AuthResponseDTO;
import com.crt.server.dto.PagedResponseDTO;
import com.crt.server.dto.UserDTO;
import com.crt.server.exception.ResourceNotFoundException;
import com.crt.server.model.Branch;
import com.crt.server.model.Role;
import com.crt.server.model.User;
import com.crt.server.repository.UserRepository;
import com.crt.server.service.EmailService;
import com.crt.server.service.UserService;
import com.crt.server.util.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Override
    public UserDTO createUser(UserDTO createUserDTO) {
        log.info("Creating user with username: {} and email: {}", createUserDTO.getUsername(), createUserDTO.getEmail());

        // Validate input
        if (createUserDTO.getUsername() == null || createUserDTO.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Username cannot be null or empty");
        }
        if (createUserDTO.getEmail() == null || createUserDTO.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email cannot be null or empty");
        }

        // Trim whitespace
        String username = createUserDTO.getUsername().trim();
        String email = createUserDTO.getEmail().trim().toLowerCase();

        boolean usernameExists = existsByUsername(username);
        boolean emailExists = existsByEmail(email);

        log.info("Username '{}' exists: {}", username, usernameExists);
        log.info("Email '{}' exists: {}", email, emailExists);

        if (usernameExists) {
            log.error("Username already exists: {}", username);
            throw new RuntimeException("Username already exists");
        }

        if (emailExists) {
            log.error("Email already exists: {}", email);
            throw new RuntimeException("Email already exists");
        }

        String generatedPassword = PasswordGenerator.generatePassword();
        log.info("Generated password for user: {}", username);
        
        User user = User.builder()
                .name(createUserDTO.getName())
                .email(email)
                .phone(createUserDTO.getPhone())
                .username(username)
                .password(passwordEncoder.encode(generatedPassword))
                .employeeId(createUserDTO.getEmployeeId())
                .role(createUserDTO.getRole())
                .department(createUserDTO.getDepartment())
                .isFirstLogin(true)
                .isActive(createUserDTO.getIsActive() != null ? createUserDTO.getIsActive() : true)
                .build();
        
        log.info("Created user with branch: {}", user.getDepartment());

        log.info("About to save user: {}", user.getUsername());

        try {
            // Save user
            User savedUser = userRepository.save(user);
            log.info("Successfully saved user with ID: {}", savedUser.getId());

            // Send email with credentials (non-critical - don't fail if email fails)
            try {
                log.info("Sending password email to: {}", savedUser.getEmail());
                emailService.sendPasswordEmail(savedUser.getEmail(), savedUser.getUsername(), generatedPassword);
                log.info("Password email sent successfully");
            } catch (Exception emailException) {
                log.warn("Failed to send password email to {}: {}", savedUser.getEmail(), emailException.getMessage());
                // Don't fail user creation if email fails
            }

            return convertToDTO(savedUser);
            
        } catch (Exception e) {
            log.error("Failed to save user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create user: " + e.getMessage());
        }
    }

    @Override
    public UserDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertToDTO(user);
    }

    @Override
    public User getFacById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
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
    public PagedResponseDTO<UserDTO> getUsersPaginated(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<User> userPage = userRepository.findAll(pageable);
        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return PagedResponseDTO.<UserDTO>builder()
                .content(userDTOs)
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .hasNext(userPage.hasNext())
                .hasPrevious(userPage.hasPrevious())
                .build();
    }

    @Override
    public List<UserDTO> getAllFacs() {
        return userRepository.findActiveUsersByRole(Role.FACULTY).stream()
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
        if (updateUserDTO.getIsActive() != null) {
            user.setActive(updateUserDTO.getIsActive());
        }

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

    @Override
    public void updateFirstLoginStatus(String email, boolean isFirstLogin) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        user.setFirstLogin(isFirstLogin);
        userRepository.save(user);

    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .username(user.getUsername())
                .department(user.getDepartment() )
                .role(user.getRole())
                .isFirstLogin(user.isFirstLogin())
                .employeeId(user.getEmployeeId())
                .isActive(user.isActive())
                .build();
    }
}
