package com.crt.server.service;

import com.crt.server.model.User;

public interface CurrentUserService {

    User getCurrentUser();
    

    String getCurrentUsername();
}
