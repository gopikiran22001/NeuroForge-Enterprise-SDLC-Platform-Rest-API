package com.stdace.neuroforge.service.user;

import com.stdace.neuroforge.enums.UserRole;
import com.stdace.neuroforge.enums.UserStatus;
import com.stdace.neuroforge.common.PageResponse;
import com.stdace.neuroforge.dto.user.UserRequest;
import com.stdace.neuroforge.dto.user.UserResponse;

import java.util.UUID;

public interface UserService {

    UserResponse create(UserRequest request);

    UserResponse getById(UUID id);

    PageResponse<UserResponse> search(String search, UserStatus status, UserRole role, int page, int size);

    UserResponse update(UUID id, UserRequest request);

    void delete(UUID id);
}
