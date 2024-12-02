package com.example.seclibtestapp.oauth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.seclib.config.csrf.CsrfBypass;
import com.seclib.exception.OAuthException;
import com.seclib.socialLogin.*;
import com.seclib.user.dto.SocialLoginUserDTO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/oauth")
@Slf4j
public class OAuthController {

    private final GoogleOAuthClient googleOAuthClient;
    private final GitHubOAuthClient gitHubOAuthClient;
    private final DefaultSocialLoginService socialLoginService;
    private SocialLoginUserDTO user;

    public OAuthController(GoogleOAuthClient googleOAuthClient, GitHubOAuthClient gitHubOAuthClient, DefaultSocialLoginService socialLoginService) {
        this.googleOAuthClient = googleOAuthClient;
        this.gitHubOAuthClient = gitHubOAuthClient;
        this.socialLoginService = socialLoginService;
    }

    @CsrfBypass
    @GetMapping("/google")
    public String redirectToGoogleAuthorization(@RequestParam("frontendRedirectUri") String frontendRedirectUri, HttpServletResponse response) {
        String authorizationUrl = googleOAuthClient.buildAuthorizationUrl(response, frontendRedirectUri);
        log.info("Redirecting to Google Authorization URL: {}", authorizationUrl);
        return "redirect:" + authorizationUrl;
    }


    @CsrfBypass
    @GetMapping("/google/callback")
    public ResponseEntity<Void> handleGoogleCallback(@RequestParam("code") String code, @RequestParam(value = "state", required = false) String state, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (state == null) {
                log.error("State parameter is missing in the callback");
                throw new OAuthException(400, "State parameter is missing");
            }
            String frontendRedirectUri = googleOAuthClient.retrieveDataFromState(state, request);
            if (frontendRedirectUri == null && (googleOAuthClient.getConfiguredState() == null || googleOAuthClient.getConfiguredState().isEmpty())) {
                throw new OAuthException(400, "Invalid state parameter");
            }
            log.info("Handling Google callback with code: {}", code);
            TokenResponse tokenResponse = googleOAuthClient.exchangeCodeForToken(code);
            GoogleUserProfile userProfile = googleOAuthClient.fetchUserProfile(tokenResponse.getAccessToken());
            log.info("DATA GOT FROM LOGING:");
            log.info(userProfile.getSub());
            log.info(userProfile.getName());
            log.info(userProfile.getEmail());

            SocialLoginUserDTO user = socialLoginService.loginViaSocial(userProfile, Optional.of("USER"), request);
            String redirectUri = (frontendRedirectUri != null ? frontendRedirectUri : "/defaultRedirect") + "?username=" + user.getUsername();
            log.info("REDIRECT URI: {}", redirectUri);

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

    private String generateNonce() {
        return UUID.randomUUID().toString();
    }

    private void storeNonceInCookie(String nonce, String frontendRedirectUri, HttpServletResponse response) {
        Cookie cookie = new Cookie(nonce, frontendRedirectUri);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(300);
        response.addCookie(cookie);
    }

    private String retrieveFrontendRedirectUriFromCookie(String nonce, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(nonce)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
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
