package com.example.seclibtestapp.user;

import com.seclib.Totp.service.DefaultTotpService;
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

    public UserController(DefaultUserService userService, DefaultTotpService totpService) {
        this.userService = userService;
        this.totpService = totpService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody UserDTO userDTO, HttpSession session)
            throws InterruptedException {
        System.out.println("user register: " + userDTO.username());
        System.out.println("password: " + userDTO.password());
        DefaultUser userToRegister = userService.register(userDTO.username(), userDTO.password(), "USER");

        byte[] qrCode = totpService.generateQRCodeImage(userToRegister.getTotpSecret(), 200, 200);
        String encodedQrCode = Base64.getEncoder().encodeToString(qrCode);

        Map<String, String> response = new HashMap<>();
        response.put("qrCode", encodedQrCode);
        response.put("totpSecret", userToRegister.getTotpSecret());
        System.out.println("REGISTER FINISHED");

        return ResponseEntity.ok(response);

    }

    @PostMapping("/login")
    public ResponseEntity<DefaultUser> login(@RequestBody UserDTO userDTO, HttpSession session, HttpServletRequest request)
            throws InterruptedException {
        System.out.println("user login: " + userDTO.username());
        System.out.println("password: " + userDTO.password());
        System.out.println("totp: " + userDTO.totp());
        DefaultUser user = userService.login(userDTO.username(), userDTO.password(), userDTO.totp(), session, request);
        session.setAttribute("userId", user.getId());
        System.out.println("User id: " + session.getAttribute("userId"));
        System.out.println("LOGIN FINISHED");
        return ResponseEntity.ok(user);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody UserDTO userDTO, HttpSession session)
            throws InterruptedException {
        String token = userService.forgotPassword(userDTO.username());
        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        /*mailService.sendEmail(userDTO.username(), "Password Reset", "Click the following link to reset your password: " + resetLink); */
        System.out.println("Reset link: " + resetLink);
        System.out.println("FORGOT PASSWORD FINISHED");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody PasswordResetDTO passwordResetDTO, HttpSession session)
            throws IOException, InterruptedException {
        if (!passwordResetDTO.newPassword().equals(passwordResetDTO.confirmPassword())) {
            throw new IOException("Passwords do not match");
        }
        userService.resetPassword(passwordResetDTO.token(), passwordResetDTO.newPassword());
        System.out.println("RESET PASSWORD FINISHED");
        return ResponseEntity.ok().build();
    }

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
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        System.out.println("LOGOUT FINISHED");
        return ResponseEntity.ok().build();
    }
}