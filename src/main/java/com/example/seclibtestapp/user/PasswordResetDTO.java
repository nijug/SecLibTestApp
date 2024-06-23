package com.example.seclibtestapp.user;

public record PasswordResetDTO( String token, String newPassword, String confirmPassword) {
}
