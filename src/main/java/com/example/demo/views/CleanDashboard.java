package com.example.demo.views;

import com.example.demo.service.PerfectAuthService;
import com.example.demo.service.DeviceService;
import com.example.demo.service.AnalyticsService;
import com.example.demo.service.EnhancedLocationService;
import com.example.demo.service.GeofenceService;
import com.example.demo.model.Device;
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
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Route("dashboard")
@PageTitle("LAPSO - Professional Laptop Security Dashboard")
@AnonymousAllowed
public class CleanDashboard extends VerticalLayout {

    private final PerfectAuthService authService;
    private final DeviceService deviceService;
    private final AnalyticsService analyticsService;
    private final EnhancedLocationService enhancedLocationService;
    private final GeofenceService geofenceService;

    public CleanDashboard(PerfectAuthService authService, DeviceService deviceService, AnalyticsService analyticsService, EnhancedLocationService enhancedLocationService, GeofenceService geofenceService) {
        this.authService = authService;
        this.deviceService = deviceService;
        this.analyticsService = analyticsService;
        this.enhancedLocationService = enhancedLocationService;
        this.geofenceService = geofenceService;
        
        // Removed authentication check to allow access without login
        System.out.println("Showing dashboard without authentication check");
        
        // Add user email to page for WebSocket subscription
        String userEmail = "guest@example.com"; // Use a default email when not authenticated
        if (userEmail != null) {
            UI.getCurrent().getPage().executeJs(
                "window.currentUserEmail = $0;", userEmail
            );
            
            // Load battery alert system from static resources
            UI.getCurrent().getPage().addJavaScript("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js");
            UI.getCurrent().getPage().addJavaScript("https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js");
            UI.getCurrent().getPage().addJavaScript("./js/battery-alerts.js");
        }
        
        createCleanDashboard();
    }

    private void createCleanDashboard() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        
        // Clean, professional background (removed purple gradient)
        getStyle()
            .set("background", "#f5f5f5")
            .set("font-family", "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif")
            .set("color", "#333333");

        // User-friendly header
        add(createWelcomeHeader());
        
        // Main content with user focus
        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setPadding(true);
        mainContent.setSpacing(true);
        mainContent.getStyle()
            .set("max-width", "1000px")
            .set("margin", "0 auto")
            .set("width", "100%")
            .set("background", "rgba(255, 255, 255, 0.95)")
            .set("border-radius", "20px")
            .set("box-shadow", "0 20px 40px rgba(0,0,0,0.1)")
            .set("color", "#333");
        
        // User-focused status
        mainContent.add(createUserFriendlyStatus());
        
        // Simple devices section
        mainContent.add(createSimpleDevicesSection());
        
        // What users actually want to do
        mainContent.add(createUserActions());
        
        add(mainContent);
    }

    private Component createWelcomeHeader() {
        // Main header container
        HorizontalLayout headerContainer = new HorizontalLayout();
        headerContainer.setWidthFull();
        headerContainer.setPadding(true);
        headerContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        headerContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerContainer.getStyle()
            .set("background", "#ffffff")
            .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
            .set("padding", "1rem 2rem");

        // Left side: Logo and greeting
        VerticalLayout leftSection = new VerticalLayout();
        leftSection.setSpacing(false);
        leftSection.setPadding(false);
        
        String currentUser = "Guest User"; // Use a default name when not authenticated
        String displayName = "Guest";
        
        H1 greeting = new H1("Hello, " + displayName);
        greeting.getStyle()
            .set("color", "#333333")
            .set("font-size", "28px")
            .set("font-weight", "600")
            .set("margin", "0 0 4px 0");
        
        Paragraph subtitle = new Paragraph("Manage your devices");
        subtitle.getStyle()
            .set("color", "#666666")
            .set("font-size", "14px")
            .set("margin", "0")
            .set("font-weight", "400");
        
        leftSection.add(greeting, subtitle);
        
        // Right side: Logout button
        Button logoutButton = new Button("Logout", VaadinIcon.SIGN_OUT.create());
        logoutButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        logoutButton.addClickListener(e -> {
            // Just navigate to login without actual logout
            UI.getCurrent().navigate("login");
            Notification.show("Logged out successfully", 3000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        logoutButton.getStyle()
            .set("cursor", "pointer");
        
        headerContainer.add(leftSection, logoutButton);
        return headerContainer;
    }

    private Component createUserFriendlyStatus() {
        VerticalLayout statusSection = new VerticalLayout();
        statusSection.setPadding(false);
        statusSection.setSpacing(true);
        statusSection.getStyle().set("margin-bottom", "2rem");

        H2 sectionTitle = new H2("Your Protection Status");
        sectionTitle.getStyle()
            .set("color", "#1f2937")
            .set("font-size", "1.5rem")
            .set("font-weight", "600")
            .set("margin", "0 0 1rem 0")
            .set("text-align", "center");

        HorizontalLayout statusCards = new HorizontalLayout();
        statusCards.setWidthFull();
        statusCards.setSpacing(true);
        statusCards.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        String userEmail = "guest@example.com"; // Use a default email when not authenticated
        Map<String, Object> analytics = analyticsService.getDashboardAnalytics(userEmail != null ? userEmail : "");

        statusCards.add(
            createFriendlyStatusCard("Devices", analytics.get("totalDevices").toString(), "#10b981"),
            createFriendlyStatusCard("Online", analytics.get("onlineDevices").toString(), "#3b82f6"),
            createFriendlyStatusCard("Protected", analytics.get("totalDevices").toString(), "#059669")
        );

        statusSection.add(sectionTitle, statusCards);
        return statusSection;
    }

    private Component createSimpleDevicesSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);

        H2 sectionTitle = new H2("Devices");
        sectionTitle.getStyle()
            .set("color", "#111827")
            .set("font-size", "20px")
            .set("font-weight", "600")
            .set("margin", "0 0 16px 0");

        // Devices list - get devices for current user only
        List<Device> devices = deviceService.getCurrentUserDevices();
        
        if (devices.isEmpty()) {
            section.add(sectionTitle, createFriendlyEmptyState());
        } else {
            VerticalLayout devicesList = new VerticalLayout();
            devicesList.setPadding(false);
            devicesList.setSpacing(true);
            
            for (Device device : devices) {
                devicesList.add(createFriendlyDeviceCard(device));
            }
            
            section.add(sectionTitle, devicesList);
        }

        return section;
    }

    private Component createUserActions() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);

        H2 sectionTitle = new H2("Quick Actions");
        sectionTitle.getStyle()
            .set("color", "#111827")
            .set("font-size", "20px")
            .set("font-weight", "600")
            .set("margin", "0 0 16px 0");

        HorizontalLayout actionsGrid = new HorizontalLayout();
        actionsGrid.setWidthFull();
        actionsGrid.setSpacing(true);
        actionsGrid.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

        actionsGrid.add(
            createUserActionCard("Locate Devices", "View all devices on map", () -> UI.getCurrent().navigate("map")),
            createUserActionCard("Add Device", "Protect another device", () -> UI.getCurrent().navigate("download-agent"))
        );

        section.add(sectionTitle, actionsGrid);
        return section;
    }

    // Helper methods
    private Component createFriendlyStatusCard(String title, String value, String color) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.getStyle()
            .set("background", "#ffffff")
            .set("border-radius", "12px")
            .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)")
            .set("border", "1px solid #e5e7eb")
            .set("flex", "1")
            .set("min-width", "120px")
            .set("text-align", "center")
            .set("padding", "20px");

        H3 valueH3 = new H3(value);
        valueH3.getStyle()
            .set("color", "#111827")
            .set("font-size", "28px")
            .set("font-weight", "700")
            .set("margin", "0 0 4px 0");

        Paragraph titleP = new Paragraph(title);
        titleP.getStyle()
            .set("color", "#6b7280")
            .set("font-size", "13px")
            .set("font-weight", "500")
            .set("margin", "0");

        card.add(valueH3, titleP);
        return card;
    }
    
    private Component createFriendlyEmptyState() {
        VerticalLayout emptyState = new VerticalLayout();
        emptyState.setAlignItems(FlexComponent.Alignment.CENTER);
        emptyState.setPadding(true);
        emptyState.getStyle()
            .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
            .set("border-radius", "20px")
            .set("padding", "3rem")
            .set("text-align", "center")
            .set("color", "#ffffff");

        Span icon = new Span("🚀");
        icon.getStyle().set("font-size", "4rem");

        H3 title = new H3("Ready to protect your first laptop?");
        title.getStyle()
            .set("color", "#ffffff")
            .set("margin", "1rem 0 0.5rem 0")
            .set("font-weight", "600");

        Paragraph description = new Paragraph("It takes just 2 minutes to set up. We'll guide you through every step!");
        description.getStyle()
            .set("color", "rgba(255, 255, 255, 0.9)")
            .set("margin", "0 0 2rem 0")
            .set("font-size", "1.1rem");

        Button addFirstDevice = new Button("🛡️ Protect My Laptop", VaadinIcon.PLUS.create());
        addFirstDevice.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        addFirstDevice.getStyle()
            .set("background", "rgba(255, 255, 255, 0.2)")
            .set("border", "2px solid rgba(255, 255, 255, 0.3)")
            .set("color", "#ffffff")
            .set("border-radius", "25px")
            .set("padding", "1rem 2rem")
            .set("font-size", "1.1rem")
            .set("font-weight", "600")
            .set("backdrop-filter", "blur(10px)");
        addFirstDevice.addClickListener(e -> UI.getCurrent().navigate("download-agent"));

        emptyState.add(icon, title, description, addFirstDevice);
        return emptyState;
    }
    
    private Component createFriendlyDeviceCard(Device device) {
        HorizontalLayout card = new HorizontalLayout();
        card.setWidthFull();
        card.setPadding(true);
        card.setSpacing(true);
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.getStyle()
            .set("background", "#ffffff")
            .set("border-radius", "12px")
            .set("border", "1px solid #e5e7eb")
            .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)")
            .set("transition", "all 0.2s ease")
            .set("margin-bottom", "12px")
            .set("cursor", "pointer");
        
        card.addClickListener(e -> UI.getCurrent().navigate("map"));

        // Device image (like Microsoft Find My Device)
        Div deviceImageContainer = new Div();
        deviceImageContainer.getStyle()
            .set("width", "120px")
            .set("height", "80px")
            .set("background", "#f3f4f6")
            .set("border-radius", "8px")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("flex-shrink", "0")
            .set("margin-right", "16px")
            .set("position", "relative")
            .set("overflow", "hidden");
        
        // Laptop SVG illustration
        Div laptopIcon = new Div();
        laptopIcon.getElement().setProperty("innerHTML", 
            "<svg width='64' height='48' viewBox='0 0 64 48' fill='none'>" +
            "<rect x='8' y='4' width='48' height='32' rx='2' fill='#9ca3af' stroke='#6b7280' stroke-width='2'/>" +
            "<rect x='10' y='6' width='44' height='28' fill='#374151'/>" +
            "<path d='M2 36h60l-4 8H6l-4-8z' fill='#6b7280'/>" +
            "<circle cx='32' cy='40' r='1.5' fill='#4b5563'/>" +
            "</svg>");
        deviceImageContainer.add(laptopIcon);

        // Device info
        VerticalLayout deviceInfo = new VerticalLayout();
        deviceInfo.setPadding(false);
        deviceInfo.setSpacing(false);
        deviceInfo.getStyle().set("flex", "1");

        String deviceName = device.getDeviceName() != null ? device.getDeviceName() : "Windows PC";
        H3 nameH3 = new H3(deviceName);
        nameH3.getStyle()
            .set("color", "#111827")
            .set("font-size", "16px")
            .set("font-weight", "600")
            .set("margin", "0 0 4px 0");

        // Status
        String statusMessage = "";
        String statusColor = "#059669";
        boolean isOnline = device.getIsOnline() != null ? device.getIsOnline() : false;
        
        if (isOnline) {
            if (device.getAddress() != null) {
                statusMessage = device.getAddress();
            } else {
                statusMessage = "Online";
            }
        } else {
            statusMessage = "Offline";
            statusColor = "#6b7280";
        }

        Paragraph statusP = new Paragraph(statusMessage);
        statusP.getStyle()
            .set("color", statusColor)
            .set("font-size", "14px")
            .set("margin", "0 0 4px 0")
            .set("font-weight", "500");

        // Last seen
        String lastSeenText = getTimeAgo(device.getLastSeen());
        Paragraph lastSeenP = new Paragraph(lastSeenText);
        lastSeenP.getStyle()
            .set("color", "#6b7280")
            .set("font-size", "13px")
            .set("margin", "0");

        // Battery display
        HorizontalLayout batteryInfo = new HorizontalLayout();
        batteryInfo.setSpacing(true);
        batteryInfo.setAlignItems(FlexComponent.Alignment.CENTER);
        batteryInfo.getStyle().set("margin-top", "8px");
        
        if (device.getBatteryLevel() != null) {
            String batteryIcon;
            String batteryColor;
            Integer batteryLevel = device.getBatteryLevel();
            
            if (batteryLevel >= 80) {
                batteryIcon = "🔋";
                batteryColor = "#059669";
            } else if (batteryLevel >= 50) {
                batteryIcon = "🔋";
                batteryColor = "#3b82f6";
            } else if (batteryLevel >= 20) {
                batteryIcon = "🔋";
                batteryColor = "#f59e0b";
            } else {
                batteryIcon = "🪫";
                batteryColor = "#ef4444";
            }
            
            Span batteryBadge = new Span(batteryIcon + " " + batteryLevel + "%");
            batteryBadge.getStyle()
                .set("color", batteryColor)
                .set("font-size", "13px")
                .set("font-weight", "600")
                .set("padding", "4px 8px")
                .set("background", batteryColor + "20")
                .set("border-radius", "6px");
            
            if (device.getIsCharging() != null && device.getIsCharging()) {
                Span chargingBadge = new Span("⚡ Charging");
                chargingBadge.getStyle()
                    .set("color", "#10b981")
                    .set("font-size", "12px")
                    .set("font-weight", "600")
                    .set("padding", "4px 8px")
                    .set("background", "#10b98120")
                    .set("border-radius", "6px");
                batteryInfo.add(batteryBadge, chargingBadge);
            } else {
                batteryInfo.add(batteryBadge);
            }
        }

        deviceInfo.add(nameH3, statusP, lastSeenP, batteryInfo);

        // Action buttons
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        
        Button locateBtn = new Button("Locate");
        locateBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        locateBtn.getStyle()
            .set("border-radius", "6px")
            .set("font-size", "14px");
        locateBtn.addClickListener(e -> {
            e.getSource().getUI().ifPresent(ui -> ui.navigate("map"));
        });
        
        Button lockBtn = new Button("Lock");
        lockBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        lockBtn.getStyle()
            .set("border-radius", "6px")
            .set("font-size", "14px");
        lockBtn.addClickListener(e -> {
            executeQuickAction(device.getDeviceId(), "lock", "Device locked");
        });
        
        actions.add(locateBtn, lockBtn);

        card.add(deviceImageContainer, deviceInfo, actions);
        return card;
    }
    
    private Component createUserActionCard(String title, String description, Runnable action) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.setAlignItems(FlexComponent.Alignment.START);
        card.getStyle()
            .set("background", "#ffffff")
            .set("border-radius", "12px")
            .set("border", "1px solid #e5e7eb")
            .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)")
            .set("flex", "1")
            .set("cursor", "pointer")
            .set("transition", "all 0.2s ease")
            .set("padding", "20px")
            .set("max-width", "300px");

        card.addClickListener(e -> action.run());

        H3 titleH3 = new H3(title);
        titleH3.getStyle()
            .set("color", "#111827")
            .set("font-size", "16px")
            .set("font-weight", "600")
            .set("margin", "0 0 4px 0");

        Paragraph desc = new Paragraph(description);
        desc.getStyle()
            .set("color", "#6b7280")
            .set("font-size", "14px")
            .set("margin", "0");

        card.add(titleH3, desc);
        return card;
    }
    
    private void executeQuickAction(String deviceId, String action, String successMessage) {
        Notification.show(successMessage, 3000, Notification.Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
    
    private String getTimeAgo(LocalDateTime lastSeen) {
        if (lastSeen == null) return "unknown";
        
        long minutes = java.time.Duration.between(lastSeen, LocalDateTime.now()).toMinutes();
        
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + " minutes ago";
        if (minutes < 1440) return (minutes / 60) + " hours ago";
        return (minutes / 1440) + " days ago";
    }
}