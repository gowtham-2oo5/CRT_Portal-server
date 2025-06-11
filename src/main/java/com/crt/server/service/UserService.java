package com.crt.server.service;

import com.crt.server.dto.AuthResponseDTO;
import com.crt.server.dto.UpdatePasswordDTO;
import com.crt.server.dto.UserDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserDTO createUser(UserDTO createUserDTO);

    UserDTO getUserById(UUID id);

    UserDTO getUserByUsername(String username);

    UserDTO getUserByEmail(String email);

    List<UserDTO> getAllUsers();

    UserDTO updateUser(UUID id, UserDTO updateUserDTO);

    void deleteUser(UUID id);

    // Password management

    void resetPassword(String email);

    // Additional utility methods
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    AuthResponseDTO updatePassword(UUID id, String currentPassword, String newPassword);

    AuthResponseDTO updatePasswordByEmail(String email, String newPassword);
}