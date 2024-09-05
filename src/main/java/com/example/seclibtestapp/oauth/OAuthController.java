package com.example.seclibtestapp.oauth;

import com.seclib.socialLogin.GoogleOAuthClient;
import com.seclib.socialLogin.TokenResponse;
import com.seclib.socialLogin.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
@RequestMapping("/oauth")
@Slf4j
public class OAuthController {

    private final GoogleOAuthClient googleOAuthClient;

    public OAuthController(GoogleOAuthClient googleOAuthClient) {
        this.googleOAuthClient = googleOAuthClient;
    }

    @GetMapping("/google")
    public String redirectToGoogleAuthorization() {
        // Generate a secure random state (omitted for this test)
        String authorizationUrl = googleOAuthClient.buildAuthorizationUrl("testState");
        return "redirect:" + authorizationUrl;
    }

    @GetMapping("/google/callback")
    @ResponseBody
    public String handleGoogleCallback(@RequestParam("code") String code) {
        try {
            TokenResponse tokenResponse = googleOAuthClient.exchangeCodeForToken(code);
            // Fetch the user profile
            UserProfile userProfile = googleOAuthClient.fetchUserProfile(tokenResponse.getAccessToken());
            log.info("DATA GOT FROM LOGING:");
            log.info(userProfile.getEmail());
            System.out.println("DATA GOT FROM LOGING:");
            return "User Profile: " + userProfile;
        } catch (IOException e) {
            // Log the exception and return an error message
            log.error("Error during Google OAuth callback", e);
            return "Error during Google OAuth callback";
        }
    }
}
