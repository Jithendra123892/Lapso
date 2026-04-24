package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class AuthController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    // Removed root mapping to avoid conflicts with Vaadin routing
    // Vaadin will handle all routing including the root path
    
    // Removed conflicting login mapping - Vaadin PerfectLoginView handles /login
    
    // Removed manual login handler - now using Spring Security authentication
    
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
    
    @PostMapping("/register")
    public String register(@RequestParam String email, @RequestParam String password, 
                          @RequestParam String confirmPassword, HttpSession session, Model model) {
        
        if (email == null || !email.contains("@")) {
            model.addAttribute("error", "Please enter a valid email");
            return "register";
        }
        
        if (password == null || password.length() < 4) {
            model.addAttribute("error", "Password must be at least 4 characters");
            return "register";
        }
        
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "register";
        }
        
        try {
            // Check if user already exists
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                model.addAttribute("error", "User with this email already exists");
                return "register";
            }
            
            // Create new user with proper password encoding using UserService
            User newUser = userService.registerManualUser(email, email.split("@")[0], password);
            
            // Note: Removed auto-login after registration to use proper Spring Security flow
            // User should login manually after registration
            model.addAttribute("success", "Registration successful! Please login.");
            return "login";
            
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }
    
    @GetMapping("/logout")
    public RedirectView logout(HttpSession session) {
        session.invalidate();
        return new RedirectView("/login");
    }
}
