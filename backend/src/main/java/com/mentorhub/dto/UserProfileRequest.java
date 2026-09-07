package com.mentorhub.dto;

import lombok.Data;

/**
 * UserProfileRequest DTO - used for editing common user profile fields (name, phone, bio).
 * Both students and teachers can update these via PUT /api/user/profile.
 */
@Data
public class UserProfileRequest {
    private String name;
    private String phone;
    private String bio;
}
