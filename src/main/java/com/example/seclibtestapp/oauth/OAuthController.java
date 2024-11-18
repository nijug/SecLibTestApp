package com.example.seclibtestapp.oauth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.seclib.config.csrf.CsrfBypass;
import com.seclib.exception.OAuthException;
import com.seclib.socialLogin.*;
import com.seclib.user.dto.SocialLoginUserDTO;
import com.seclib.user.model.SocialLoginUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.text.ParseException;
import java.util.Optional;

@Controller
@RequestMapping("/oauth")
@Slf4j
public class OAuthController {

    private final GoogleOAuthClient googleOAuthClient;
    private final GitHubOAuthClient gitHubOAuthClient;
    private final DefaultSocialLoginService socialLoginService;
    private String frontendRedirectUri;
    private SocialLoginUserDTO user;


    public OAuthController(GoogleOAuthClient googleOAuthClient, GitHubOAuthClient gitHubOAuthClient, DefaultSocialLoginService socialLoginService) {
        this.googleOAuthClient = googleOAuthClient;
        this.gitHubOAuthClient = gitHubOAuthClient;
        this.socialLoginService = socialLoginService;
    }

    @CsrfBypass
    @GetMapping("/google")
    public String redirectToGoogleAuthorization(@RequestParam("frontendRedirectUri") String frontendRedirectUri) {
        String authorizationUrl = googleOAuthClient.buildAuthorizationUrl();
        this.frontendRedirectUri = frontendRedirectUri;
        return "redirect:" + authorizationUrl;
    }

    @CsrfBypass
    @GetMapping("/google/callback")
    public ResponseEntity<Void> handleGoogleCallback(@RequestParam("code") String code, HttpServletRequest request) {
        try {
            TokenResponse tokenResponse = googleOAuthClient.exchangeCodeForToken(code);
            GoogleUserProfile userProfile = googleOAuthClient.fetchUserProfile(tokenResponse.getAccessToken());
            log.info("DATA GOT FROM LOGING:");
            log.info(userProfile.getSub());
            log.info(userProfile.getName());
            log.info(userProfile.getEmail());

            SocialLoginUserDTO user = socialLoginService.loginViaSocial(userProfile, Optional.of("USER"),request);
            this.user = user;
            String redirectUri = frontendRedirectUri + "?username=" + user.getUsername();
            System.out.println("REDIRECT URI: " + redirectUri);

            return ResponseEntity.status(302).header("Location", redirectUri).build();
        } catch (IOException | ParseException | BadJOSEException | JOSEException e) {
            log.error("Error during Google OAuth callback", e);
            throw new OAuthException(401, "Error during Google OAuth callback");
        }
    }

    @CsrfBypass
    @GetMapping("/oauth-callback")
    public ResponseEntity<SocialLoginUserDTO> getUserInfo(@RequestParam("username") String username) {
        System.out.println("USER INFO: " + this.user);
        return Optional.ofNullable(this.user)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).build());
    }
/*
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

            SocialLoginUser user = socialLoginService.loginViaSocial(userProfile);
            return "User Profile: " + user;
        } catch (IOException e) {
            log.error("Error during GitHub OAuth callback", e);
            return "Error during GitHub OAuth callback";
        }
    }
*/
}
