package com.edunexus.dto;

import com.edunexus.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** Used by Admin to provision internal accounts (GBR-02: SME/Teacher/Course Manager cannot self-register). */
@Getter
@Setter
public class AdminUserForm {
    @NotBlank
    private String name;
    @NotBlank
    @Email
    private String email;
    private Role role = Role.SME;
}
