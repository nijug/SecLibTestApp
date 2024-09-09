package com.example.seclibtestapp.oauth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.seclib.exception.OAuthException;
import com.seclib.socialLogin.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.text.ParseException;

@Controller
@RequestMapping("/oauth")
@Slf4j
public class OAuthController {

    private final GoogleOAuthClient googleOAuthClient;
    private final GitHubOAuthClient gitHubOAuthClient;

    public OAuthController(GoogleOAuthClient googleOAuthClient, GitHubOAuthClient gitHubOAuthClient) {
        this.googleOAuthClient = googleOAuthClient;
        this.gitHubOAuthClient = gitHubOAuthClient;
    }

    @GetMapping("/google")
    public String redirectToGoogleAuthorization() {
        String authorizationUrl = googleOAuthClient.buildAuthorizationUrl();
        return "redirect:" + authorizationUrl;
    }

    @GetMapping("/google/callback")
    @ResponseBody
    public String handleGoogleCallback(@RequestParam("code") String code) {
        try {
            TokenResponse tokenResponse = googleOAuthClient.exchangeCodeForToken(code);
            GoogleUserProfile userProfile = googleOAuthClient.fetchUserProfile(tokenResponse.getAccessToken());
            log.info("DATA GOT FROM LOGING:");
            log.info(userProfile.getSub());
            log.info(userProfile.getName());
            log.info(userProfile.getEmail());
            return "User Profile: " + userProfile;
        } catch (IOException e) {
            log.error("Error during Google OAuth callback", e);
            throw new OAuthException(401, "Error during Google OAuth callback");
        } catch (ParseException | BadJOSEException | JOSEException e) {
            log.error("Error validating ID token", e);
            throw new OAuthException(401, "Invalid ID token");
        }
    }

    @GetMapping("/github")
    public String redirectToGitHubAuthorization() {
        String authorizationUrl = gitHubOAuthClient.buildAuthorizationUrl();
        return "redirect:" + authorizationUrl;
    }

    @GetMapping("/github/callback")
    @ResponseBody
    public String handleGitHubCallback(@RequestParam("code") String code) {
        try {
            TokenResponse tokenResponse = gitHubOAuthClient.exchangeCodeForToken(code);
            GitHubUserProfile userProfile = gitHubOAuthClient.fetchUserProfile(tokenResponse.getAccessToken());
            log.info("DATA GOT FROM LOGING:");
            log.info(userProfile.getId());
            log.info(userProfile.getLogin());
            log.info(userProfile.getEmail());
            return "User Profile: " + userProfile;
        } catch (IOException e) {
            log.error("Error during GitHub OAuth callback", e);
            return "Error during GitHub OAuth callback";
        }
    }
}