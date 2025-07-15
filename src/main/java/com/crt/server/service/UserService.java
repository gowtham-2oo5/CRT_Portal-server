package com.crt.server.service;

import com.crt.server.dto.AuthResponseDTO;
import com.crt.server.dto.UserDTO;
import com.crt.server.model.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserDTO createUser(UserDTO createUserDTO);

    UserDTO getUserById(UUID id);

    User getFacById(UUID id);

    UserDTO getUserByUsername(String username);

    UserDTO getUserByEmail(String email);

    List<UserDTO> getAllUsers();
    List<UserDTO> getAllFacs();



    UserDTO updateUser(UUID id, UserDTO updateUserDTO);

    void deleteUser(UUID id);

    // Password management
    void resetPassword(String email);

    // Additional utility methods
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    AuthResponseDTO updatePassword(UUID id, String currentPassword, String newPassword);

    AuthResponseDTO updatePasswordByEmail(String email, String newPassword);

    // Update first login status
    void updateFirstLoginStatus(String email, boolean isFirstLogin);
}
