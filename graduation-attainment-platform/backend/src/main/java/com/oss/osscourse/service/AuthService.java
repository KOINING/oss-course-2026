package com.oss.osscourse.service;

import com.oss.osscourse.dto.LoginRequest;
import com.oss.osscourse.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
