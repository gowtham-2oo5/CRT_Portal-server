package com.crt.server.dto;

import com.crt.server.model.Role;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDTO {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String department;
    private String employeeId;
    private String username;
    private Role role;
    private Boolean isFirstLogin;
    private Boolean isActive;
}
