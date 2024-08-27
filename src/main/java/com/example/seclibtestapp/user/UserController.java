package com.example.seclibtestapp.user;

import com.seclib.config.csrf.CsrfBypass;
import com.seclib.totp.DefaultTotpService;
import com.seclib.user.dto.DefaultUserDTO;
import com.seclib.user.mapper.DefaultUserMapper;
import com.seclib.user.model.DefaultUser;
import com.seclib.user.service.DefaultUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final DefaultUserService userService;
    private final DefaultTotpService totpService;
    private final DefaultUserMapper mapper;

    public UserController(DefaultUserService userService, DefaultTotpService totpService, DefaultUserMapper mapper) {
        this.userService = userService;
        this.totpService = totpService;
        this.mapper = mapper;
    }

    @PostMapping("/register")
    @CsrfBypass
    public ResponseEntity<Map<String, String>> register(@RequestBody DefaultUserDTO userDTO)
            throws InterruptedException {
        System.out.println("user register: " + userDTO.getUsername());
        System.out.println("password: " + userDTO.getPassword());
        DefaultUserDTO userToRegister = userService.register(userDTO.getUsername(), userDTO.getPassword(), "USER");

        byte[] qrCode = totpService.generateQRCodeImage(userToRegister.getTotpSecret(), 200, 200);
        String encodedQrCode = Base64.getEncoder().encodeToString(qrCode);

        Map<String, String> response = new HashMap<>();
        response.put("qrCode", encodedQrCode);
        response.put("totpSecret", userToRegister.getTotpSecret());
        System.out.println("REGISTER FINISHED");

        return ResponseEntity.ok(response);

    }

    @PostMapping("/login")
    @CsrfBypass
    public ResponseEntity<DefaultUserDTO > login(@RequestBody DefaultUserDTO userDTO, HttpServletRequest request)
            throws InterruptedException {
        System.out.println("user login: " + userDTO.getUsername());
        System.out.println("password: " + userDTO.getPassword());
        System.out.println("totp: " + userDTO.getTotpSecret());
        DefaultUserDTO loggedUser = userService.login(userDTO, request);
        System.out.println("LOGIN FINISHED");
        return ResponseEntity.ok(loggedUser);
    }

    @PostMapping("/forgot-password")
    @CsrfBypass
    public ResponseEntity<Void> forgotPassword(@RequestBody DefaultUserDTO userDTO)
            throws InterruptedException {
        String token = userService.forgotPassword(userDTO.getUsername());
        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        /*mailService.sendEmail(userDTO.username(), "Password Reset", "Click the following link to reset your password: " + resetLink); */
        System.out.println("Reset link: " + resetLink);
        System.out.println("FORGOT PASSWORD FINISHED");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @CsrfBypass
    public ResponseEntity<Void> resetPassword(@RequestBody PasswordResetDTO passwordResetDTO)
            throws IOException, InterruptedException {
        if (!passwordResetDTO.newPassword().equals(passwordResetDTO.confirmPassword())) {
            throw new IOException("Passwords do not match");
        }
        userService.resetPassword(passwordResetDTO.token(), passwordResetDTO.newPassword());
        System.out.println("RESET PASSWORD FINISHED");
        return ResponseEntity.ok().build();
    }

    /* todo: think about this method, probably should be part of lib */
    @GetMapping("/check-authentication")
    public ResponseEntity<Map<String, String>> checkAuthentication(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            DefaultUser user = userService.findById(userId);
            Map<String, String> response = new HashMap<>();
            response.put("username", user.getUsername());
            System.out.println("CHECK AUTHENTICATION FINISHED");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        userService.logout(request);
        System.out.println("LOGOUT FINISHED");
        return ResponseEntity.ok().build();
    }

    /*this is fine for debug i guess, delete latter */

    @CsrfBypass
    @PostMapping("/debug/register-login-admin")
    public ResponseEntity<DefaultUserDTO >  debugRegisterLoginAdmin(HttpServletRequest request) throws InterruptedException {
        System.out.println("Debug register-login admin endpoint called");

        String adminUsername = "admin-debug";
        String adminPassword = "adminPassword123@";
        String adminRole = "ADMIN";

        System.out.println("Checking if admin user exists");
        DefaultUser adminUser = userService.findByUsername(adminUsername);
        DefaultUserDTO admin;
        if (adminUser == null) {
            System.out.println("Admin user does not exist, registering new admin user");
            admin = userService.register(adminUsername, adminPassword, adminRole);
            admin.setPassword(adminPassword);
            System.out.println("Admin user registered with username: " + adminUsername);
        } else {
            admin = mapper.toDefaultUserDTO(adminUser);
            admin.setPassword(adminPassword);
            System.out.println("Admin user already exists with username: " + adminUsername);
        }

        System.out.println("Generating TOTP code for admin user");
        String totpCode = totpService.generateCurrentNumber(admin.getTotpSecret());
        admin.setTotpSecret(totpCode);
        System.out.println("Performing login for admin user");
        admin = userService.login(admin,request);

        System.out.println("Setting session attributes for admin user");

        System.out.println("Admin user logged in successfully");


        return ResponseEntity.ok(admin);
    }

}