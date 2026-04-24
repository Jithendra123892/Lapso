package com.example.demo.views;

import com.example.demo.model.Device;
import com.example.demo.service.DeviceService;
import com.example.demo.service.PerfectAuthService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Route("remote-control")
@PageTitle("Remote Control - LAPSO")
@AnonymousAllowed
public class RemoteControlView extends VerticalLayout {

    private final PerfectAuthService authService;
    private final DeviceService deviceService;

    private ComboBox<Device> deviceSelector;
    private VerticalLayout commandHistoryLayout;

    public RemoteControlView(PerfectAuthService authService, DeviceService deviceService) {
        this.authService = authService;
        this.deviceService = deviceService;

        // Removed authentication check to allow access without login
        System.out.println("Showing remote control interface without authentication check");
        createRemoteControlInterface();
    }

    private void createRemoteControlInterface() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        // Header
        add(createHeader());

        // Main content
        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setSizeFull();
        mainContent.setPadding(true);
        mainContent.setSpacing(true);
        mainContent.getStyle()
            .set("background", "rgba(255, 255, 255, 0.95)")
            .set("border-radius", "20px 20px 0 0")
            .set("flex", "1")
            .set("overflow-y", "auto");

        // Device selector
        deviceSelector = createDeviceSelector();
        mainContent.add(deviceSelector);

        // Command buttons grid
        mainContent.add(createCommandGrid());

        // Command history
        VerticalLayout commandHistoryLayout = new VerticalLayout();
        commandHistoryLayout.setPadding(true);
        commandHistoryLayout.setSpacing(true);
        commandHistoryLayout.getStyle()
            .set("background", "#f9fafb")
            .set("border-radius", "12px")
            .set("margin-top", "24px");

        H3 historyTitle = new H3("Command History");
        historyTitle.getStyle().set("margin", "0 0 16px 0");
        commandHistoryLayout.add(historyTitle);

        mainContent.add(commandHistoryLayout);

        add(mainContent);
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.setSpacing(true);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle()
            .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
            .set("color", "#ffffff")
            .set("padding", "1rem 2rem");

        Button backBtn = new Button(" Dashboard", VaadinIcon.ARROW_LEFT.create());
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backBtn.getStyle()
            .set("color", "#ffffff")
            .set("border", "1px solid rgba(255, 255, 255, 0.3)")
            .set("border-radius", "20px");
        backBtn.addClickListener(e -> UI.getCurrent().navigate("dashboard"));

        H1 title = new H1("Remote Control");
        title.getStyle()
            .set("color", "#ffffff")
            .set("font-size", "20px")
            .set("font-weight", "600")
            .set("margin", "0")
            .set("flex", "1");

        header.add(backBtn, title);
        return header;
    }

    private ComboBox<Device> createDeviceSelector() {
        ComboBox<Device> selector = new ComboBox<>("Select Device");
        selector.setWidthFull();
        selector.setItemLabelGenerator(device -> 
            device.getDeviceName() + " (" + (device.getIsOnline() ? "Online" : "Offline") + ")"
        );

        List<Device> devices = deviceService.getCurrentUserDevices();
        selector.setItems(devices);

        if (!devices.isEmpty()) {
            selector.setValue(devices.get(0));
        }

        selector.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                loadCommandHistory(e.getValue());
            }
        });

        return selector;
    }

    private Component createCommandGrid() {
        VerticalLayout grid = new VerticalLayout();
        grid.setPadding(false);
        grid.setSpacing(true);

        H3 commandsTitle = new H3("Available Commands");
        commandsTitle.getStyle().set("margin", "0 0 16px 0");
        grid.add(commandsTitle);

        HorizontalLayout row1 = new HorizontalLayout();
        row1.setWidthFull();
        row1.setSpacing(true);
        row1.add(
            createCommandCard("Lock Device", "Lock the device screen (PIN required to unlock)", "LOCK", "#e74c3c"),
            createCommandCard("Sound Alarm", "Play loud alarm sound (10 seconds)", "ALARM", "#f39c12")
        );

        HorizontalLayout row2 = new HorizontalLayout();
        row2.setWidthFull();
        row2.setSpacing(true);
        row2.add(
            createCommandCard("Screenshot", "Capture current screen", "SCREENSHOT", "#3498db"),
            createCommandCard("Camera Photo", "Capture photo from device camera", "CAMERA", "#9b59b6")
        );

        HorizontalLayout row3 = new HorizontalLayout();
        row3.setWidthFull();
        row3.setSpacing(true);
        row3.add(
            createCommandCard("Locate Device", "Request current location", "LOCATE", "#16a085"),
            createCommandCard("Display Message", "Show message on screen", "MESSAGE", "#1abc9c")
        );

        HorizontalLayout row4 = new HorizontalLayout();
        row4.setWidthFull();
        row4.setSpacing(true);
        row4.add(
            createCommandCard("Unlock Device", "Unlock the device remotely", "UNLOCK", "#27ae60"),
            createCommandCard("Wipe Data", "DANGEROUS: Erase all data", "WIPE", "#c0392b")
        );

        grid.add(row1, row2, row3, row4);
        return grid;
    }

    private Component createCommandCard(String title, String description, String commandType, String color) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
            .set("background", "#ffffff")
            .set("border-radius", "12px")
            .set("border", "2px solid " + color)
            .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
            .set("flex", "1")
            .set("min-width", "280px")
            .set("cursor", "pointer")
            .set("transition", "all 0.2s");

        H4 cardTitle = new H4(title);
        cardTitle.getStyle()
            .set("color", color)
            .set("margin", "0 0 8px 0")
            .set("font-size", "16px")
            .set("font-weight", "600");

        Paragraph cardDesc = new Paragraph(description);
        cardDesc.getStyle()
            .set("color", "#6b7280")
            .set("font-size", "13px")
            .set("margin", "0 0 12px 0");

        Button actionBtn = new Button("Execute");
        actionBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        actionBtn.getStyle()
            .set("background", color)
            .set("border-radius", "6px");

        actionBtn.addClickListener(e -> {
            if (commandType.equals("MESSAGE")) {
                showMessageDialog();
            } else if (commandType.equals("WIPE")) {
                showWipeConfirmation();
            } else {
                executeCommand(commandType, null);
            }
        });

        card.add(cardTitle, cardDesc, actionBtn);
        return card;
    }

    private void showMessageDialog() {
        // Create a simple dialog for entering message
        TextArea messageArea = new TextArea("Message to display");
        messageArea.setWidthFull();
        messageArea.setPlaceholder("Enter message to display on device screen...");
        messageArea.setMaxLength(500);

        Button sendBtn = new Button("Send Message", e -> {
            String message = messageArea.getValue();
            if (message != null && !message.trim().isEmpty()) {
                executeCommand("MESSAGE", Map.of("message", message));
            }
        });
        sendBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // For now, just execute directly (in real app, show a dialog)
        Notification.show("Message dialog would appear here", 3000, Notification.Position.MIDDLE);
    }

    private void showWipeConfirmation() {
        Notification notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.setPosition(Notification.Position.MIDDLE);
        notification.setDuration(0); // Stay open

        Div text = new Div(new Span("WARNING: This will PERMANENTLY erase all data on the device. This action CANNOT be undone!"));
        text.getStyle().set("font-weight", "bold");

        Button confirmBtn = new Button("Yes, Wipe Device", e -> {
            executeCommand("WIPE", null);
            notification.close();
        });
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> notification.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout actions = new HorizontalLayout(confirmBtn, cancelBtn);
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        actions.setSpacing(true);

        VerticalLayout layout = new VerticalLayout(text, actions);
        notification.add(layout);
        notification.open();
    }

    private void executeCommand(String commandType, Map<String, String> params) {
        Device selectedDevice = deviceSelector.getValue();
        if (selectedDevice == null) {
            Notification.show("Please select a device first", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> request = new java.util.HashMap<>();
            request.put("deviceId", selectedDevice.getDeviceId());
            request.put("commandType", commandType);
            request.put("priority", commandType.equals("WIPE") ? 10 : 5);
            
            if (params != null) {
                request.put("commandParams", new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(params));
            }

            restTemplate.postForEntity(
                "http://localhost:8080/api/remote-commands/send",
                request,
                Map.class
            );

            Notification.show(
                "Command sent: " + commandType,
                3000,
                Notification.Position.TOP_CENTER
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Reload history
            loadCommandHistory(selectedDevice);

        } catch (Exception e) {
            Notification.show(
                "Failed to send command: " + e.getMessage(),
                5000,
                Notification.Position.TOP_CENTER
            ).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void loadCommandHistory(Device device) {
        commandHistoryLayout.removeAll();
        
        H3 historyTitle = new H3("Command History");
        historyTitle.getStyle().set("margin", "0 0 16px 0");
        commandHistoryLayout.add(historyTitle);

        commandHistoryLayout.add(new Paragraph("Command history will appear here..."));
    }
}
