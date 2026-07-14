package com.stdace.neuroforge.service.auth;

import com.stdace.neuroforge.enums.OrganizationStatus;
import com.stdace.neuroforge.enums.OrganizationType;
import com.stdace.neuroforge.enums.UserRole;
import com.stdace.neuroforge.enums.UserStatus;
import com.stdace.neuroforge.exception.DuplicateResourceException;
import com.stdace.neuroforge.exception.ResourceNotFoundException;
import com.stdace.neuroforge.models.Organization;
import com.stdace.neuroforge.models.RefreshToken;
import com.stdace.neuroforge.models.User;
import com.stdace.neuroforge.dto.auth.AuthResponse;
import com.stdace.neuroforge.dto.auth.LoginRequest;
import com.stdace.neuroforge.dto.auth.RefreshTokenRequest;
import com.stdace.neuroforge.dto.auth.RegisterRequest;
import com.stdace.neuroforge.exception.BusinessException;
import com.stdace.neuroforge.exception.UnauthorizedException;
import com.stdace.neuroforge.mapper.UserMapper;
import com.stdace.neuroforge.repository.RefreshTokenRepository;
import com.stdace.neuroforge.repository.UserRepository;
import com.stdace.neuroforge.security.CurrentUserUtil;
import com.stdace.neuroforge.security.JwtService;
import com.stdace.neuroforge.security.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final com.stdace.neuroforge.repository.OrganizationRepository organizationRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CookieUtil cookieUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }
        if (request.getRole() == UserRole.SUPER_ADMIN) {
            throw new BusinessException("You are not allowed to register as SUPER_ADMIN");
        }

        User user = userMapper.toEntity(request, passwordEncoder);

        if (request.getRole() == UserRole.ORG_ADMIN) {
            // Validate organization details
            if (request.getOrgName() == null || request.getOrgName().isBlank()) {
                throw new BusinessException("Organization name is required for ORG_ADMIN registration");
            }
            if (request.getOrgSlug() == null || request.getOrgSlug().isBlank()) {
                throw new BusinessException("Organization slug is required for ORG_ADMIN registration");
            }
            if (organizationRepository.existsByNameIgnoreCase(request.getOrgName())) {
                throw new DuplicateResourceException("Organization name already exists: " + request.getOrgName());
            }
            if (organizationRepository.existsBySlugIgnoreCase(request.getOrgSlug())) {
                throw new DuplicateResourceException("Organization slug already taken: " + request.getOrgSlug());
            }

            // Create pending organization
            Organization org = new com.stdace.neuroforge.models.Organization();
            org.setName(request.getOrgName());
            org.setSlug(request.getOrgSlug().toLowerCase());
            org.setType(request.getOrgType() != null ? request.getOrgType() : OrganizationType.STARTUP);
            org.setDescription(request.getOrgDescription());

            // Save organization first to generate ID
            org = organizationRepository.save(org);

            // Set user status to pending and link organization
            user.setOrganization(org);
            user = userRepository.save(user);

            // Link owner back to organization
            org.setOwner(user);
            organizationRepository.save(org);
        } else {
            // Normal user registering below ORG_ADMIN
            if (request.getOrganizationId() == null) {
                throw new BusinessException("Organization selection is required");
            }
            com.stdace.neuroforge.models.Organization org = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Selected organization not found"));

            if (org.getStatus() != OrganizationStatus.ACTIVE) {
                throw new BusinessException("Selected organization is not active");
            }

            user.setOrganization(org);
            user = userRepository.save(user);
        }

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        Object principal = authentication.getPrincipal();
        User user = principal instanceof com.stdace.neuroforge.security.CustomUserDetails customUserDetails
                ? customUserDetails.getUser()
                : userRepository.findByEmailIgnoreCase(((UserDetails) principal).getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (user.getStatus() == UserStatus.PENDING_APPROVAL) {
            throw new UnauthorizedException("Your account is pending approval by an administrator.");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("User account is not active");
        }
        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String email = jwtService.extractUsername(request.getRefreshToken());
        if (!jwtService.isRefreshToken(request.getRefreshToken())) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse refreshToken(HttpServletRequest request) {
        // Extract refresh token from secure HTTP-only cookie
        String refreshToken = cookieUtil.getRefreshTokenFromCookies(request)
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found in cookies"));

        // Validate token type and extract email
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String email = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        return buildAuthResponse(user);
    }

    public void logout(HttpServletResponse response) {
        UUID userId = CurrentUserUtil.getCurrentUserId();
        if (userId != null) {
            RefreshToken refreshToken =  refreshTokenRepository.findByUserId(userId)
                    .orElseThrow(() -> new UnauthorizedException("Refresh token not found for user"));
            refreshTokenRepository.delete(refreshToken);
        }
        cookieUtil.clearAuthenticationCookies(response);
    }

    private AuthResponse buildAuthResponse(User user) {
        if (user.getStatus() == UserStatus.PENDING_APPROVAL) {
            return AuthResponse.builder()
                    .user(userMapper.toResponse(user))
                    .build();
        }
        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .tokenType("Bearer")
                .expiresInSeconds(3600)
                .user(userMapper.toResponse(user))
                .build();
    }
}
