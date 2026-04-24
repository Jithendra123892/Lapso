package com.example.demo.security;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;

import java.util.Optional;

@Service
public class SecurityService {

    public Optional<UserDetails> getAuthenticatedUserDetails() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null && context.getAuthentication() != null 
            && context.getAuthentication().isAuthenticated()
            && context.getAuthentication().getPrincipal() instanceof UserDetails) {
            return Optional.of((UserDetails) context.getAuthentication().getPrincipal());
        }
        return Optional.empty();
    }

    public Optional<OAuth2User> getAuthenticatedOAuth2User() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null && context.getAuthentication() != null 
            && context.getAuthentication().isAuthenticated()
            && context.getAuthentication().getPrincipal() instanceof OAuth2User) {
            return Optional.of((OAuth2User) context.getAuthentication().getPrincipal());
        }
        return Optional.empty();
    }

    public String getCurrentUserEmail() {
        // First try OAuth2
        try {
            Optional<OAuth2User> oauth2User = getAuthenticatedOAuth2User();
            if (oauth2User.isPresent() && oauth2User.get().getAttribute("email") != null) {
                String email = oauth2User.get().getAttribute("email");
                System.out.println("🔑 Found OAuth2 user email: " + email);
                return email;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error getting OAuth2 user: " + e.getMessage());
        }
        
        // Then try Spring Security UserDetails
        try {
            Optional<UserDetails> userDetails = getAuthenticatedUserDetails();
            if (userDetails.isPresent()) {
                String username = userDetails.get().getUsername();
                System.out.println("🔐 Found Spring Security user: " + username);
                return username;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error getting Spring Security user: " + e.getMessage());
        }
        
        // Check HTTP session
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            if (attr != null) {
                HttpSession httpSession = attr.getRequest().getSession(false);
                if (httpSession != null) {
                    // Check Spring Security context first
                    SecurityContext context = (SecurityContext) httpSession.getAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
                    if (context != null && context.getAuthentication() != null 
                            && context.getAuthentication().isAuthenticated()) {
                        String username = context.getAuthentication().getName();
                        System.out.println("🌐 Found user in HTTP session context: " + username);
                        return username;
                    }
                    
                    // Check manual session attributes
                    String sessionUser = (String) httpSession.getAttribute("authenticated_user");
                    if (sessionUser != null) {
                        System.out.println("🔍 Found user in HTTP session: " + sessionUser);
                        return sessionUser;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Could not check HTTP session for user: " + e.getMessage());
        }
        
        // Fallback: check Vaadin session for manual login
        try {
            com.vaadin.flow.server.VaadinSession vaadinSession = 
                com.vaadin.flow.server.VaadinSession.getCurrent();
            if (vaadinSession != null) {
                // Check if we have a linked HTTP session
                String httpSessionId = (String) vaadinSession.getAttribute("httpSessionId");
                if (httpSessionId != null) {
                    try {
                        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                        if (attr != null) {
                            HttpSession httpSession = attr.getRequest().getSession(false);
                            if (httpSession != null && httpSession.getId().equals(httpSessionId)) {
                                SecurityContext context = (SecurityContext) httpSession.getAttribute(
                                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
                                if (context != null && context.getAuthentication() != null 
                                        && context.getAuthentication().isAuthenticated()) {
                                    String username = context.getAuthentication().getName();
                                    System.out.println("🔗 Found user in linked HTTP session: " + username);
                                    return username;
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️ Could not check linked HTTP session for user: " + e.getMessage());
                    }
                }
                
                // Fall back to direct Vaadin session check
                String sessionUser = (String) vaadinSession.getAttribute("authenticated_user");
                if (sessionUser != null) {
                    System.out.println("📦 Found user in Vaadin session: " + sessionUser);
                    return sessionUser;
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Could not check Vaadin session for user: " + e.getMessage());
        }
        
        System.out.println("🔍 No user found in any session or context");
        return null;
    }

    public String getCurrentUserName() {
        Optional<OAuth2User> oauth2User = getAuthenticatedOAuth2User();
        if (oauth2User.isPresent()) {
            return oauth2User.get().getAttribute("name");
        }
        
        Optional<UserDetails> userDetails = getAuthenticatedUserDetails();
        if (userDetails.isPresent()) {
            return userDetails.get().getUsername();
        }
        
        return null;
    }

    public boolean isAuthenticated() {
        // Check Spring Security authentication context
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null && context.getAuthentication() != null 
            && context.getAuthentication().isAuthenticated()
            && !"anonymousUser".equals(context.getAuthentication().getName())) {
            System.out.println("🔒 User authenticated via Spring Security context: " + 
                context.getAuthentication().getName());
            return true;
        }
        
        // Check HTTP session
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            if (attr != null) {
                HttpSession httpSession = attr.getRequest().getSession(false);
                if (httpSession != null) {
                    SecurityContext sessionContext = (SecurityContext) httpSession.getAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
                    if (sessionContext != null && sessionContext.getAuthentication() != null 
                            && sessionContext.getAuthentication().isAuthenticated()) {
                        System.out.println("🌐 User authenticated via HTTP session: " + 
                            sessionContext.getAuthentication().getName());
                        return true;
                    }
                    
                    // Check for manual session attributes
                    Boolean userAuth = (Boolean) httpSession.getAttribute("user_authenticated");
                    String sessionUser = (String) httpSession.getAttribute("authenticated_user");
                    if (userAuth != null && userAuth && sessionUser != null) {
                        System.out.println("🔑 User authenticated via manual HTTP session: " + sessionUser);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Could not check HTTP session: " + e.getMessage());
        }
        
        // Fallback: check Vaadin session for manual login
        try {
            com.vaadin.flow.server.VaadinSession vaadinSession = 
                com.vaadin.flow.server.VaadinSession.getCurrent();
            if (vaadinSession != null) {
                // Check if we have a linked HTTP session
                String httpSessionId = (String) vaadinSession.getAttribute("httpSessionId");
                if (httpSessionId != null) {
                    try {
                        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                        if (attr != null) {
                            HttpSession httpSession = attr.getRequest().getSession(false);
                            if (httpSession != null && httpSession.getId().equals(httpSessionId)) {
                                SecurityContext linkedContext = (SecurityContext) httpSession.getAttribute(
                                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
                                if (linkedContext != null && linkedContext.getAuthentication() != null 
                                        && linkedContext.getAuthentication().isAuthenticated()) {
                                    System.out.println("🔗 User authenticated via linked HTTP session: " + 
                                        linkedContext.getAuthentication().getName());
                                    return true;
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️ Could not check linked HTTP session: " + e.getMessage());
                    }
                }
                
                // Fall back to direct Vaadin session check
                Boolean userAuth = (Boolean) vaadinSession.getAttribute("user_authenticated");
                String sessionUser = (String) vaadinSession.getAttribute("authenticated_user");
                if (userAuth != null && userAuth && sessionUser != null) {
                    System.out.println("📦 User authenticated via direct Vaadin session: " + sessionUser);
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Could not check Vaadin session: " + e.getMessage());
        }
        
        System.out.println("🔐 No active authentication found in any session");
        return false;
    }

    public void logout() {
        SecurityContextHolder.clearContext();
    }
}
