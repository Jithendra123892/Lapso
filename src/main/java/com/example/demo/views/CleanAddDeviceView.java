package com.example.demo.views;

import com.example.demo.service.PerfectAuthService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Route("add-device")
@PageTitle("Secure Your Laptop - LAPSO Professional")
@AnonymousAllowed

public class CleanAddDeviceView extends VerticalLayout {

    private final PerfectAuthService authService;

    public CleanAddDeviceView(PerfectAuthService authService) {
        this.authService = authService;
        
        // Removed authentication check to allow access without login
        System.out.println("Showing add device interface without authentication check");
        createCleanAddDeviceInterface();
    }

    private void createCleanAddDeviceInterface() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        
        // Clean modern background
        getStyle()
            .set("background", "#f8fafc")
            .set("font-family", "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif");

        // Header
        add(createCleanHeader());
        
        // Main content
        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setPadding(true);
        mainContent.setSpacing(true);
        mainContent.getStyle()
            .set("max-width", "800px")
            .set("margin", "0 auto")
            .set("width", "100%");
        
        // Hero section
        mainContent.add(createHeroSection());
        
        // Download section
        mainContent.add(createDownloadSection());
        
        // Instructions
        mainContent.add(createInstructionsSection());
        
        add(mainContent);
    }

    private Component createCleanHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.setSpacing(true);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.getStyle()
            .set("background", "#ffffff")
            .set("border-bottom", "1px solid #e5e7eb")
            .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)");

        // Back button and title
        HorizontalLayout leftSection = new HorizontalLayout();
        leftSection.setAlignItems(FlexComponent.Alignment.CENTER);
        leftSection.setSpacing(true);
        
        Button backBtn = new Button("← Back", VaadinIcon.ARROW_LEFT.create());
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backBtn.addClickListener(e -> UI.getCurrent().navigate(""));
        
        H1 title = new H1("Add Device");
        title.getStyle()
            .set("color", "#1f2937")
            .set("font-size", "1.5rem")
            .set("font-weight", "700")
            .set("margin", "0");
        
        leftSection.add(backBtn, title);

        // Show generic user info when not authenticated
        Span userInfo = new Span("👤 Guest User");
        userInfo.getStyle()
            .set("color", "#6b7280")
            .set("font-weight", "500");

        header.add(leftSection, userInfo);
        return header;
    }

    private Component createHeroSection() {
        VerticalLayout hero = new VerticalLayout();
        hero.setAlignItems(FlexComponent.Alignment.CENTER);
        hero.setPadding(true);
        hero.getStyle()
            .set("background", "#ffffff")
            .set("border-radius", "1rem")
            .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)")
            .set("text-align", "center")
            .set("margin-bottom", "2rem");

        Span icon = new Span("🛡️");
        icon.getStyle().set("font-size", "4rem");

        H1 title = new H1("Add Your Device to LAPSO");
        title.getStyle()
            .set("color", "#1f2937")
            .set("font-size", "2.5rem")
            .set("font-weight", "800")
            .set("margin", "1rem 0 0.5rem 0");

        Paragraph description = new Paragraph("Simple, free device tracking - No hidden costs, completely open source 🆓");
        description.getStyle()
            .set("color", "#6b7280")
            .set("font-size", "1.125rem")
            .set("margin", "0 0 2rem 0")
            .set("max-width", "600px");

        hero.add(icon, title, description);
        return hero;
    }

    private Component createDownloadSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.setSpacing(true);
        section.getStyle()
            .set("background", "#ffffff")
            .set("border-radius", "1rem")
            .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)")
            .set("margin-bottom", "2rem");

        H2 sectionTitle = new H2("Download Agent");
        sectionTitle.getStyle()
            .set("color", "#1f2937")
            .set("font-size", "1.5rem")
            .set("font-weight", "700")
            .set("margin", "0 0 1rem 0");

        // Download cards
        HorizontalLayout downloadCards = new HorizontalLayout();
        downloadCards.setWidthFull();
        downloadCards.setSpacing(true);

        downloadCards.add(
            createDownloadCard("🪟", "Windows", "lapso-windows.exe", "Free Windows Client"),
            createDownloadCard("🍎", "macOS", "lapso-macos.dmg", "Free macOS Client"),
            createDownloadCard("🐧", "Linux", "lapso-linux.deb", "Free Linux Client")
        );

        section.add(sectionTitle, downloadCards);
        return section;
    }

    private Component createDownloadCard(String icon, String platform, String filename, String description) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.getStyle()
            .set("background", "#f8fafc")
            .set("border-radius", "0.75rem")
            .set("border", "1px solid #e5e7eb")
            .set("flex", "1")
            .set("text-align", "center")
            .set("cursor", "pointer")
            .set("transition", "all 0.2s ease");

        // Hover effect
        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle()
                .set("background", "#f1f5f9")
                .set("transform", "translateY(-2px)")
                .set("box-shadow", "0 4px 12px rgba(0, 0, 0, 0.15)");
        });
        
        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle()
                .set("background", "#f8fafc")
                .set("transform", "translateY(0)")
                .set("box-shadow", "none");
        });

        Span iconSpan = new Span(icon);
        iconSpan.getStyle().set("font-size", "3rem");

        H3 platformName = new H3(platform);
        platformName.getStyle()
            .set("color", "#1f2937")
            .set("font-size", "1.25rem")
            .set("font-weight", "600")
            .set("margin", "1rem 0 0.5rem 0");

        Paragraph desc = new Paragraph(description);
        desc.getStyle()
            .set("color", "#6b7280")
            .set("font-size", "0.875rem")
            .set("margin", "0 0 1rem 0");

        Button downloadBtn = new Button("Download", VaadinIcon.DOWNLOAD.create());
        downloadBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        downloadBtn.getStyle()
            .set("background", "#10b981")
            .set("border-radius", "0.5rem");
        
        downloadBtn.addClickListener(e -> {
            registerAndDownloadDevice(platform, filename);
        });

        card.add(iconSpan, platformName, desc, downloadBtn);
        return card;
    }
    
    private void registerAndDownloadDevice(String platform, String filename) {
        try {
            String deviceId = "LAPSO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String userEmail = authService.getLoggedInUser();
            
            // Show registration notification
            Notification.show(
                "🔄 Registering device and preparing download...", 
                3000, 
                Notification.Position.TOP_CENTER
            ).addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            
            // Trigger download using JavaScript
            String downloadUrl = String.format("/download/%s?deviceId=%s&userEmail=%s&serverUrl=http://localhost:8080", 
                platform.toLowerCase(), deviceId, userEmail);
            
            UI.getCurrent().getPage().executeJs(
                "window.open($0, '_blank');", downloadUrl
            );
            
            // Show success notification
            Notification successNotification = Notification.show(
                "🎉 Device registered successfully!\n" +
                "Device ID: " + deviceId + "\n" +
                "Platform: " + platform + "\n" +
                "Download started: " + filename, 
                8000, 
                Notification.Position.TOP_CENTER
            );
            successNotification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            // Show setup instructions
            showSetupInstructions(deviceId, platform, userEmail);
            
            System.out.println("✅ Device registered and download triggered: " + deviceId + " for " + userEmail + " on " + platform);
            
        } catch (Exception ex) {
            Notification errorNotification = Notification.show(
                "❌ Registration failed: " + ex.getMessage(), 
                5000, 
                Notification.Position.TOP_CENTER
            );
            errorNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            
            System.err.println("❌ Device registration failed: " + ex.getMessage());
        }
    }
    
    private void showSetupInstructions(String deviceId, String platform, String userEmail) {
        // Create a modal-like notification with setup instructions
        Notification setupNotification = Notification.show(
            "📋 Setup Instructions:\n" +
            "1. Download will start automatically\n" +
            "2. Run installer as administrator\n" +
            "3. Enter Device ID: " + deviceId + "\n" +
            "4. Enter Email: " + userEmail + "\n" +
            "5. Agent will start protecting your device\n" +
            "\n🛡️ Your device will appear in the dashboard once connected!", 
            15000, 
            Notification.Position.MIDDLE
        );
        setupNotification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
    }

    private Component createInstructionsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.setSpacing(true);
        section.getStyle()
            .set("background", "#ffffff")
            .set("border-radius", "1rem")
            .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)");

        H2 sectionTitle = new H2("Setup Instructions");
        sectionTitle.getStyle()
            .set("color", "#1f2937")
            .set("font-size", "1.5rem")
            .set("font-weight", "700")
            .set("margin", "0 0 1rem 0");

        // Steps
        VerticalLayout steps = new VerticalLayout();
        steps.setPadding(false);
        steps.setSpacing(true);

        steps.add(
            createInstructionStep("1", "Download", "Download the agent for your operating system"),
            createInstructionStep("2", "Install", "Run the installer with administrator privileges"),
            createInstructionStep("3", "Configure", "Enter your email and device name when prompted"),
            createInstructionStep("4", "Activate", "The agent will automatically start protecting your device")
        );

        // Current time for reference
        Paragraph timestamp = new Paragraph("Setup initiated: " + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
        timestamp.getStyle()
            .set("color", "#6b7280")
            .set("font-size", "0.875rem")
            .set("margin", "2rem 0 0 0")
            .set("text-align", "center");

        section.add(sectionTitle, steps, timestamp);
        return section;
    }

    private Component createInstructionStep(String number, String title, String description) {
        HorizontalLayout step = new HorizontalLayout();
        step.setAlignItems(FlexComponent.Alignment.CENTER);
        step.setSpacing(true);
        step.setPadding(true);
        step.getStyle()
            .set("background", "#f8fafc")
            .set("border-radius", "0.75rem")
            .set("border-left", "4px solid #10b981");

        // Step number
        Span stepNumber = new Span(number);
        stepNumber.getStyle()
            .set("background", "#10b981")
            .set("color", "#ffffff")
            .set("width", "2rem")
            .set("height", "2rem")
            .set("border-radius", "50%")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("font-weight", "700")
            .set("font-size", "0.875rem");

        // Step content
        VerticalLayout stepContent = new VerticalLayout();
        stepContent.setPadding(false);
        stepContent.setSpacing(false);

        H4 stepTitle = new H4(title);
        stepTitle.getStyle()
            .set("color", "#1f2937")
            .set("font-size", "1rem")
            .set("font-weight", "600")
            .set("margin", "0 0 0.25rem 0");

        Paragraph stepDesc = new Paragraph(description);
        stepDesc.getStyle()
            .set("color", "#6b7280")
            .set("font-size", "0.875rem")
            .set("margin", "0");

        stepContent.add(stepTitle, stepDesc);
        step.add(stepNumber, stepContent);
        return step;
    }
}
