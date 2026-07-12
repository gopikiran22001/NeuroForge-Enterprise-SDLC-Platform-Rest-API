package com.stdace.neuroforge.dto.user;

import com.stdace.neuroforge.enums.UserRole;
import com.stdace.neuroforge.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String email;

    @Size(min = 6, max = 72)
    private String password;

    @NotNull
    private UserRole role;

    @NotNull
    private UserStatus status;
}
