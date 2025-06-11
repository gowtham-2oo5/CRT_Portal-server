package com.crt.server.service;


import com.crt.server.dto.AuthResponseDTO;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    public String generateToken(AuthResponseDTO authRes);
    public String extractRole(String token);
    public boolean validateToken(String token, UserDetails userDetails);
    String extractUserName(String token);
}