package com.crt.server.service;

import com.crt.server.model.User;

public interface CurrentUserService {
    
    /**
     * Get the currently authenticated user
     * 
     * @return Current user or null if not authenticated
     */
    User getCurrentUser();
    
    /**
     * Get the username of the currently authenticated user
     * 
     * @return Current username or null if not authenticated
     */
    String getCurrentUsername();
}
