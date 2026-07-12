package com.stdace.neuroforge.service.user;

import com.stdace.neuroforge.enums.UserRole;
import com.stdace.neuroforge.enums.UserStatus;
import com.stdace.neuroforge.models.User;
import com.stdace.neuroforge.common.PageResponse;
import com.stdace.neuroforge.dto.user.UserRequest;
import com.stdace.neuroforge.dto.user.UserResponse;
import com.stdace.neuroforge.exception.DuplicateResourceException;
import com.stdace.neuroforge.exception.ResourceNotFoundException;
import com.stdace.neuroforge.mapper.UserMapper;
import com.stdace.neuroforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        User user = userMapper.toEntity(request, new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder());
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return userRepository.findById(id).map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> search(String search, UserStatus status, UserRole role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if(role != null && status != null) {
            return PageResponse.from(userRepository.findByRoleAndStatus(role, status, pageable).map(userMapper::toResponse));
        } else if (role != null) {
            return PageResponse.from(userRepository.findByRole(role, pageable).map(userMapper::toResponse));
        } else if (status != null) {
            return PageResponse.from(userRepository.findByStatus(status, pageable).map(userMapper::toResponse));

        }
        return PageResponse.from(userRepository.findAll(pageable).map(userMapper::toResponse));
    }

    @Override
    public UserResponse update(UUID id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        userMapper.updateEntity(user, request, new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
    }
}
