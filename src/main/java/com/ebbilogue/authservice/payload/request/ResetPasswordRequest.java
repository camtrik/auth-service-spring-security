package com.ebbilogue.authservice.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "E-mail cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Verification code cannot be blank")
    @Size(min = 6, max = 6, message = "Verification code must be 6 digits")
    private String code;

    @NotBlank(message = "New password cannot be blank")
    @Size(min = 6, max = 40)
    private String newPassword;
}
