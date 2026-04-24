package com.example.demo.views;

import com.example.demo.service.PerfectAuthService;
import com.example.demo.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Perfect login view - no confusion, just works
 */
@Route("login")
@PageTitle("Login")
@AnonymousAllowed
public class PerfectLoginView extends VerticalLayout {

    private final PerfectAuthService authService;
    private final UserService userService;
    private EmailField emailField;
    private PasswordField passwordField;

    public PerfectLoginView(PerfectAuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
        createLoginInterface();
    }

    private void createLoginInterface() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        
        // Clean, professional background
        getStyle()
            .set("background", "#f5f7fa")
            .set("font-family", "-apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif")
            .set("min-height", "100vh")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center");

        // Main container - single centered card without product marketing
        VerticalLayout mainContainer = new VerticalLayout();
        mainContainer.setWidth("420px");
        mainContainer.setPadding(true);
        mainContainer.setSpacing(false);
        mainContainer.setAlignItems(FlexComponent.Alignment.STRETCH);
        mainContainer.getStyle()
            .set("background", "#ffffff")
            .set("border-radius", "12px")
            .set("box-shadow", "0 6px 24px rgba(0, 0, 0, 0.06)")
            .set("padding", "2rem");

        H2 loginTitle = new H2("Sign in");
        loginTitle.getStyle()
            .set("margin", "0 0 0.5rem 0")
            .set("font-size", "2rem")
            .set("font-weight", "700")
            .set("color", "#1e293b");

        Paragraph loginSubtitle = new Paragraph("");
        loginSubtitle.getStyle()
            .set("margin", "0 0 2rem 0")
            .set("color", "#64748b")
            .set("font-size", "0.95rem");

        // Email field
        emailField = new EmailField("Email address");
        emailField.setWidthFull();
        emailField.setClearButtonVisible(true);
        emailField.setPlaceholder("you@example.com");
        emailField.getStyle()
            .set("margin-bottom", "1rem");

        // Password field
        passwordField = new PasswordField("Password");
        passwordField.setWidthFull();
        passwordField.setPlaceholder("Enter your password");
        passwordField.getStyle()
            .set("margin-bottom", "1.5rem");

        // Enter key support
        passwordField.addKeyDownListener(com.vaadin.flow.component.Key.ENTER, e -> doLogin());
        emailField.addKeyDownListener(com.vaadin.flow.component.Key.ENTER, e -> doLogin());

        // Login button
        Button loginButton = new Button("Sign In");
        loginButton.setWidthFull();
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        loginButton.getStyle()
            .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
            .set("border", "none")
            .set("border-radius", "8px")
            .set("font-weight", "600")
            .set("font-size", "1rem")
            .set("padding", "0.875rem")
            .set("margin-bottom", "1rem")
            .set("cursor", "pointer");

        loginButton.addClickListener(e -> doLogin());

        // Optional: simple register link below button, no marketing
        Button registerButton = new Button("Create account");
        registerButton.setWidthFull();
        registerButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        registerButton.addClickListener(e -> doRegister());

        mainContainer.add(loginTitle, loginSubtitle, emailField, passwordField, loginButton, registerButton);
        add(mainContainer);
    }

    // Removed marketing feature list

    private void doLogin() {
        String email = emailField.getValue();
        String password = passwordField.getValue();

        System.out.println("=== Login Attempt ===");
        System.out.println("Email: " + email);
        System.out.println("Password length: " + (password != null ? password.length() : 0));

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter email and password");
            return;
        }

        if (authService.performLogin(email, password)) {
            showSuccess("Login successful");
            
            // Debug session after login
            try {
                VaadinSession session = VaadinSession.getCurrent();
                if (session != null) {
                    System.out.println("Session after login - Authenticated: " + session.getAttribute("LAPSO_AUTHENTICATED"));
                    System.out.println("Session after login - User: " + session.getAttribute("LAPSO_USER"));
                }
            } catch (Exception e) {
                System.out.println("Error checking session after login: " + e.getMessage());
            }
            
            // Simple, reliable navigation
            System.out.println("✅ Login successful, navigating to dashboard");
            UI.getCurrent().navigate("dashboard");
        } else {
            showError("Invalid email or password. Please try again.");
        }
    }

    private void showSuccess(String message) {
        Notification.show(message, 3000, Notification.Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification.show(message, 4000, Notification.Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void doRegister() {
        String email = emailField.getValue();
        String password = passwordField.getValue();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter email and password to register");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showError("Please enter a valid email address");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters long");
            return;
        }

        try {
            // Check if user already exists
            if (userService.existsByEmail(email)) {
                showError("User already exists. Please try logging in instead.");
                return;
            }

            // Create new user
            var user = userService.registerManualUser(email, password, email.split("@")[0]);
            if (user != null) {
                showSuccess("Account created successfully! You can now log in.");
                // Clear fields
                emailField.clear();
                passwordField.clear();
            } else {
                showError("Failed to create account. Please try again.");
            }
        } catch (Exception e) {
            showError("Registration failed: " + e.getMessage());
        }
    }
}