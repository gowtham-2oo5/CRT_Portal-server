package com.crt.server.service;

import com.crt.server.dto.AuthResponseDTO;
import com.crt.server.dto.PagedResponseDTO;
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
    
    /**
     * Get paginated list of users
     * 
     * @param page Page number (0-based)
     * @param size Page size
     * @param sortBy Field to sort by
     * @param direction Sort direction (ASC or DESC)
     * @return Paginated response with user DTOs
     */
    PagedResponseDTO<UserDTO> getUsersPaginated(int page, int size, String sortBy, String direction);
    
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
