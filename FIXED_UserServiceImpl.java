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
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

        if (createUserDTO.getRole() == null) {
            throw new RuntimeException("Role cannot be null");
        }

        // Check if username or email already exists
        if (userRepository.existsByUsername(createUserDTO.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(createUserDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Generate a random password if not provided
        String generatedPassword = null;
        String username = createUserDTO.getUsername();
        if (createUserDTO.getPassword() == null || createUserDTO.getPassword().trim().isEmpty()) {
            generatedPassword = PasswordGenerator.generatePassword();
            log.info("Generated password for user: {}", username);
        }

        // Create user entity
        User user = User.builder()
                .name(createUserDTO.getName())
                .email(createUserDTO.getEmail())
                .phone(createUserDTO.getPhone())
                .username(username)
                .password(passwordEncoder.encode(generatedPassword != null ? generatedPassword : createUserDTO.getPassword()))
                .employeeId(createUserDTO.getEmployeeId())
                .role(createUserDTO.getRole())
                .department(createUserDTO.getDepartment())
                .isFirstLogin(true)
                .isActive(createUserDTO.getIsActive() != null ? createUserDTO.getIsActive() : true)
                .build();

        log.info("Created user with department: {}", user.getDepartment());

        // Save user
        User savedUser = userRepository.save(user);

        // Send email with credentials if password was generated
        if (generatedPassword != null) {
            emailService.sendPasswordEmail(savedUser.getEmail(), savedUser.getUsername(), generatedPassword);
            log.info("Sent credentials email to: {}", savedUser.getEmail());
        }

        return convertToDTO(savedUser);
    }

    @Override
    public UserDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToDTO(user);
    }

    @Override
    public User getFacById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return convertToDTO(user);
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return convertToDTO(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PagedResponseDTO<UserDTO> getUsersPaginated(int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
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
                .last(userPage.isLast())
                .build();
    }

    @Override
    public List<UserDTO> getAllFacs() {
        List<User> faculties = userRepository.findByRole(Role.FACULTY);
        return faculties.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO updateUser(UUID id, UserDTO updateUserDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Update fields
        if (updateUserDTO.getName() != null) {
            existingUser.setName(updateUserDTO.getName());
        }

        if (updateUserDTO.getEmail() != null) {
            // Check if new email already exists for another user
            if (!existingUser.getEmail().equals(updateUserDTO.getEmail()) &&
                    userRepository.existsByEmail(updateUserDTO.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            existingUser.setEmail(updateUserDTO.getEmail());
        }

        if (updateUserDTO.getPhone() != null) {
            existingUser.setPhone(updateUserDTO.getPhone());
        }

        if (updateUserDTO.getUsername() != null) {
            // Check if new username already exists for another user
            if (!existingUser.getUsername().equals(updateUserDTO.getUsername()) &&
                    userRepository.existsByUsername(updateUserDTO.getUsername())) {
                throw new RuntimeException("Username already exists");
            }
            existingUser.setUsername(updateUserDTO.getUsername());
        }

        if (updateUserDTO.getPassword() != null && !updateUserDTO.getPassword().trim().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updateUserDTO.getPassword()));
        }

        if (updateUserDTO.getRole() != null) {
            existingUser.setRole(updateUserDTO.getRole());
        }

        if (updateUserDTO.getDepartment() != null) {
            existingUser.setDepartment(updateUserDTO.getDepartment());
        }

        if (updateUserDTO.getEmployeeId() != null) {
            existingUser.setEmployeeId(updateUserDTO.getEmployeeId());
        }

        if (updateUserDTO.getIsActive() != null) {
            existingUser.setActive(updateUserDTO.getIsActive());
        }

        // Save updated user
        User updatedUser = userRepository.save(existingUser);
        return convertToDTO(updatedUser);
    }

    @Override
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Generate temporary password
        String tempPassword = PasswordGenerator.generatePassword();

        // Update user's password
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setFirstLogin(true);
        userRepository.save(user);

        // Send email with temporary password
        emailService.sendPasswordEmail(email, user.getUsername(), tempPassword);
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFirstLogin(false);
        userRepository.save(user);

        return AuthResponseDTO.builder()
                .message("Password updated successfully")
                .build();
    }

    @Override
    public AuthResponseDTO updatePasswordByEmail(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFirstLogin(false);
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

    @Override
    @Transactional
    public List<UserDTO> bulkCreateFaculties(MultipartFile file) throws Exception {
        log.info("Processing bulk faculty creation from file: {}", file.getOriginalFilename());

        List<User> createdUsers = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // Skip header row
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                try {
                    String[] parts = line.split(",");
                    if (parts.length < 4) {
                        errors.add("Invalid format in line: " + line);
                        continue;
                    }

                    String name = parts[0].trim();
                    String email = parts[1].trim();
                    String phone = parts[2].trim();
                    String username = parts[3].trim();

                    // Check if user already exists
                    if (userRepository.existsByEmail(email)) {
                        errors.add("User with email " + email + " already exists");
                        continue;
                    }

                    if (userRepository.existsByUsername(username)) {
                        errors.add("User with username " + username + " already exists");
                        continue;
                    }

                    // Generate random password
                    String password = PasswordGenerator.generatePassword();

                    // Create user
                    User user = User.builder()
                            .name(name)
                            .email(email)
                            .phone(phone)
                            .username(username)
                            .password(passwordEncoder.encode(password))
                            .role(Role.FACULTY)
                            .isFirstLogin(true)
                            .build();

                    User savedUser = userRepository.save(user);
                    createdUsers.add(savedUser);

                    // Send email with credentials
                    emailService.sendPasswordEmail(email, username, password);

                } catch (Exception e) {
                    errors.add("Error processing line: " + line + " - " + e.getMessage());
                }
            }
        }

        if (!errors.isEmpty()) {
            log.warn("Encountered {} errors during bulk faculty creation", errors.size());
            for (String error : errors) {
                log.warn(error);
            }
        }

        log.info("Successfully created {} faculty accounts", createdUsers.size());

        return createdUsers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .username(user.getUsername())
                .department(user.getDepartment())
                .role(user.getRole())
                .isFirstLogin(user.isFirstLogin())
                .employeeId(user.getEmployeeId())
                .isActive(user.isActive())
                .build();
    }
}
