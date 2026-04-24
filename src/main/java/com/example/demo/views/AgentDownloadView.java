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
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;

@Route("download-agent")
@PageTitle("Download LAPSO Agent - Start Tracking")
@AnonymousAllowed
public class AgentDownloadView extends VerticalLayout {

    private final PerfectAuthService authService;

    public AgentDownloadView(PerfectAuthService authService) {
        this.authService = authService;
        
        // Removed authentication check to allow access without login
        System.out.println("Showing agent download interface without authentication check");
        createDownloadInterface();
    }

    private void createDownloadInterface() {
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
            .set("margin", "0")
            .set("flex", "1");

        // Download options
        createDownloadOptions(mainContent);
        
        add(mainContent);
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.setSpacing(true);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle()
            .set("background", "#ffffff")
            .set("color", "#1f2937")
            .set("padding", "1rem 2rem")
            .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        // Back button
        Button backBtn = new Button("Dashboard", VaadinIcon.ARROW_LEFT.create());
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backBtn.getStyle()
            .set("color", "#374151")
            .set("border", "1px solid #d1d5db")
            .set("border-radius", "8px");
        backBtn.addClickListener(e -> UI.getCurrent().navigate("dashboard"));

        // Title
        H1 title = new H1("Agent Download");
        title.getStyle()
            .set("color", "#1f2937")
            .set("font-size", "20px")
            .set("font-weight", "600")
            .set("margin", "0")
            .set("flex", "1");

        header.add(backBtn, title);
        return header;
    }

    private void createDownloadOptions(VerticalLayout container) {
        // Welcome message
        H2 welcomeTitle = new H2("Download Agent");
        welcomeTitle.getStyle()
            .set("color", "#111827")
            .set("text-align", "center")
            .set("font-size", "24px")
            .set("font-weight", "600")
            .set("margin", "0 0 8px 0");

        Paragraph welcomeDesc = new Paragraph("Choose your platform to download the LAPSO tracking agent");
        welcomeDesc.getStyle()
            .set("color", "#6b7280")
            .set("text-align", "center")
            .set("margin", "0 0 32px 0")
            .set("font-size", "14px");

        // Version panel (Windows)
        VerticalLayout versionPanel = new VerticalLayout();
        versionPanel.setPadding(true);
        versionPanel.setSpacing(false);
        versionPanel.getStyle()
            .set("background", "#ecfeff")
            .set("border", "1px solid #a5f3fc")
            .set("border-radius", "10px")
            .set("margin-bottom", "16px");

        H4 vTitle = new H4("Windows Agent Version");
        vTitle.getStyle().set("margin", "0 0 6px 0").set("color", "#164e63");
        Paragraph vInfo = new Paragraph(fetchWindowsAgentVersionText());
        vInfo.getStyle().set("margin", "0").set("color", "#0e7490");
        versionPanel.add(vTitle, vInfo);

        // Download cards
        HorizontalLayout downloadCards = new HorizontalLayout();
        downloadCards.setWidthFull();
        downloadCards.setSpacing(true);
        downloadCards.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        downloadCards.add(
            createDownloadCard("Windows", "For Windows 10/11 laptops", "/api/agents/download/windows/lapso-installer.ps1", "lapso-installer.ps1"),
            createDownloadCard("macOS", "For MacBook and iMac", "/api/agents/download/macos/lapso-installer.sh", "lapso-installer.sh"),
            createDownloadCard("Linux", "For Ubuntu, Debian, etc.", "/api/agents/download/linux/lapso-installer.sh", "lapso-installer.sh")
        );

        // Instructions
        VerticalLayout instructions = new VerticalLayout();
        instructions.setPadding(true);
        instructions.setSpacing(true);
        instructions.getStyle()
            .set("background", "#f9fafb")
            .set("border-radius", "12px")
            .set("border", "1px solid #e5e7eb")
            .set("margin-top", "32px");

        H3 instructionsTitle = new H3("Installation Steps");
        instructionsTitle.getStyle()
            .set("color", "#111827")
            .set("font-size", "18px")
            .set("font-weight", "600")
            .set("margin", "0 0 16px 0");

        VerticalLayout stepsList = new VerticalLayout();
        stepsList.setPadding(false);
        stepsList.setSpacing(true);

        stepsList.add(
            createInstructionStep("1", "Download", "Click your device type above to download the installer"),
            createInstructionStep("2", "Install", "Run the downloaded file and follow the prompts"),
            createInstructionStep("3", "Activate", "The agent will automatically connect to your account"),
            createInstructionStep("4", "Verify", "Check that your device appears in the dashboard")
        );

        instructions.add(instructionsTitle, stepsList);

        // How to run (terminal instructions)
        H3 runTitle = new H3("How to run from terminal");
        runTitle.getStyle()
            .set("color", "#111827")
            .set("font-size", "18px")
            .set("font-weight", "600")
            .set("margin", "24px 0 8px 0");

    Paragraph runDesc = new Paragraph("You can also install the agent directly from your terminal. The installer auto-detects the device ID and will ask for your email once. Replace http://localhost:8080 with your server URL if different.");
        runDesc.getStyle()
            .set("color", "#6b7280")
            .set("font-size", "14px")
            .set("margin", "0 0 12px 0");

            Component winBlock = createCodeBlock(
                "Windows (PowerShell)",
                "$base = '" + getServerBaseUrl() + "'\n$ProgressPreference='SilentlyContinue'\nInvoke-WebRequest -UseBasicParsing -Uri \"$base/api/agents/download/windows/lapso-installer.ps1\" -OutFile 'lapso-installer.ps1'\nPowerShell -ExecutionPolicy Bypass -File .\\lapso-installer.ps1"
            );

        Component macBlock = createCodeBlock(
            "macOS (Terminal)",
            "curl -fsSL '" + getServerBaseUrl() + "/api/agents/download/macos/lapso-installer.sh' -o lapso-installer.sh\nchmod +x lapso-installer.sh\nsudo ./lapso-installer.sh"
        );

        Component linuxBlock = createCodeBlock(
            "Linux (Shell)",
            "curl -fsSL '" + getServerBaseUrl() + "/api/agents/download/linux/lapso-installer.sh' -o lapso-installer.sh\nchmod +x lapso-installer.sh\nsudo ./lapso-installer.sh"
        );

        VerticalLayout runBlocks = new VerticalLayout(runTitle, runDesc, winBlock, macBlock, linuxBlock);
        runBlocks.setPadding(true);
        runBlocks.setSpacing(false);
        runBlocks.getStyle()
            .set("background", "#ffffff")
            .set("border-radius", "12px")
            .set("border", "1px solid #e5e7eb");

    container.add(welcomeTitle, welcomeDesc, versionPanel, downloadCards, instructions, runBlocks);
        
        // Remote Control Features Section
        VerticalLayout remoteControlSection = createRemoteControlInstructions();
        container.add(remoteControlSection);
        
        // Notes about URLs and privileges
            VerticalLayout notes = new VerticalLayout();
            notes.setPadding(true);
            notes.setSpacing(false);
            notes.getStyle()
                .set("background", "#fef3c7")
                .set("border", "1px solid #fde68a")
                .set("border-radius", "8px");
            H4 notesTitle = new H4("Notes");
            notesTitle.getStyle().set("margin", "0 0 6px 0").set("color", "#92400e");
            Paragraph n1 = new Paragraph("Use a full URL (e.g., http://localhost:8080). Do not use relative paths like /api/... in PowerShell.");
            Paragraph n2 = new Paragraph("Ensure the server is running before downloading (the site should open at http://localhost:8080).");
            Paragraph n3 = new Paragraph("On Windows, run the installer in an elevated (Administrator) PowerShell for the scheduled task to install successfully.");
            Paragraph n4 = new Paragraph("If deployed behind a proxy or different host, replace http://localhost:8080 with your server URL.");
            n1.getStyle().set("margin", "0 0 4px 0");
            n2.getStyle().set("margin", "0 0 4px 0");
            n3.getStyle().set("margin", "0 0 4px 0");
            n4.getStyle().set("margin", "0");
            notes.add(notesTitle, n1, n2, n3, n4);

            container.add(notes);

        // Add uninstall section
        VerticalLayout uninstallSection = createUninstallSection();
        container.add(uninstallSection);
    }

    private String fetchWindowsAgentVersionText() {
        try {
            String base = getServerBaseUrl();
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(base + "/api/agent/version?platform=windows"))
                .GET().build();
            java.net.http.HttpResponse<String> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.Map<?,?> map = om.readValue(resp.body(), java.util.Map.class);
                Object ver = map.get("version");
                Object sha = map.get("sha256");
                return "Current: " + (ver != null ? ver.toString() : "unknown") + (sha != null ? "  •  SHA-256: " + sha.toString() : "");
            }
        } catch (Exception ignored) { }
        return "Current: unknown (server not reachable)";
    }
    
    private VerticalLayout createRemoteControlInstructions() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.setSpacing(true);
        section.getStyle()
            .set("background", "#dbeafe")
            .set("border", "1px solid #93c5fd")
            .set("border-radius", "12px")
            .set("margin-top", "32px")
            .set("color", "#1e3a8a");

        H3 title = new H3("Remote Control Features");
        title.getStyle()
            .set("color", "#1e3a8a")
            .set("font-size", "20px")
            .set("font-weight", "600")
            .set("margin", "0 0 12px 0");

        Paragraph intro = new Paragraph("The latest agent version includes remote command polling for Lock, Sound Alarm, Screenshot, Camera Photo, and Wipe features.");
        intro.getStyle()
            .set("color", "#1e40af")
            .set("font-size", "14px")
            .set("margin", "0 0 20px 0");

        // Update instructions
        VerticalLayout updateBox = new VerticalLayout();
        updateBox.setPadding(true);
        updateBox.setSpacing(true);
        updateBox.getStyle()
            .set("background", "#ffffff")
            .set("border", "1px solid #93c5fd")
            .set("border-radius", "10px");

        H4 updateTitle = new H4("How to Update Your Agent:");
        updateTitle.getStyle()
            .set("color", "#1e3a8a")
            .set("font-weight", "bold")
            .set("margin", "0 0 12px 0");

        Div updateSteps = new Div();
        updateSteps.getElement().setProperty("innerHTML",
            "<ol style='margin:0;padding-left:20px;color:#1e40af;line-height:2;'>" +
            "<li><strong>Download:</strong> Get the latest agent from download buttons above</li>" +
            "<li><strong>Stop:</strong> Stop your current agent (Ctrl+C in terminal)</li>" +
            "<li><strong>Run:</strong> Execute the new agent with your device ID and email</li>" +
            "<li><strong>Verify:</strong> Check for \"Agent is running - sending heartbeats and polling for commands\" message</li>" +
            "</ol>");

        updateBox.add(updateTitle, updateSteps);

        // Features list
        VerticalLayout featuresBox = new VerticalLayout();
        featuresBox.setPadding(true);
        featuresBox.setSpacing(true);
        featuresBox.getStyle()
            .set("background", "#ffffff")
            .set("border", "1px solid #93c5fd")
            .set("border-radius", "10px")
            .set("margin-top", "12px");

        H4 featuresTitle = new H4("Available Remote Commands:");
        featuresTitle.getStyle()
            .set("color", "#1e3a8a")
            .set("font-weight", "bold")
            .set("margin", "0 0 12px 0");

        Div featuresList = new Div();
        featuresList.getElement().setProperty("innerHTML",
            "<ul style='margin:0;padding-left:20px;color:#1e40af;line-height:2.2;'>" +
            "<li><strong>Remote Lock:</strong> Instantly lock device screen with PIN/Password required to unlock</li>" +
            "<li><strong>Sound Alarm:</strong> Play loud beeping sound to help locate device (10 seconds)</li>" +
            "<li><strong>Screenshot:</strong> Capture screen remotely (appears in viewer after 32 seconds)</li>" +
            "<li><strong>Camera Photo:</strong> Capture photo from device camera (falls back to screenshot if no camera)</li>" +
            "<li><strong>Emergency Wipe:</strong> Securely erase all user data with 3 overwrite passes (DANGEROUS - requires confirmation)</li>" +
            "<li><strong>Locate Device:</strong> Get current GPS/IP-based location with 3-5 meter accuracy</li>" +
            "</ul>");

        featuresBox.add(featuresTitle, featuresList);

        // Access instructions
        Div accessNote = new Div();
        accessNote.getElement().setProperty("innerHTML",
            "<p style='margin:12px 0 0 0;padding:12px;background:#eff6ff;border-radius:8px;border-left:4px solid #3b82f6;color:#1e40af;'>" +
            "<strong>How to Access:</strong> After updating your agent, open the Map view in LAPSO. Click any device marker to see remote control buttons. Commands are executed when the agent polls the server (every 30 seconds).</p>");

        section.add(title, intro, updateBox, featuresBox, accessNote);
        return section;
    }

    private VerticalLayout createUninstallSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.setSpacing(true);
        section.getStyle()
            .set("background", "#fef2f2")
            .set("border", "1px solid #fecaca")
            .set("border-radius", "12px")
            .set("margin-top", "32px");

        H3 uninstallTitle = new H3("How to Uninstall");
        uninstallTitle.getStyle()
            .set("color", "#991b1b")
            .set("font-size", "18px")
            .set("font-weight", "600")
            .set("margin", "0 0 8px 0");

        Paragraph uninstallDesc = new Paragraph("If you need to remove the LAPSO agent, each installer automatically creates an uninstall script:");
        uninstallDesc.getStyle()
            .set("color", "#7f1d1d")
            .set("font-size", "14px")
            .set("margin", "0 0 16px 0");

        // Windows uninstall
        Component winUninstall = createUninstallBlock(
            "Windows",
            "Run PowerShell as Administrator, then execute:",
            "& \"C:\\Program Files\\LAPSO\\uninstall.ps1\""
        );

        // macOS uninstall
        Component macUninstall = createUninstallBlock(
            "macOS",
            "Run in Terminal:",
            "sudo /usr/local/lapso/uninstall.sh"
        );

        // Linux uninstall
        Component linuxUninstall = createUninstallBlock(
            "Linux",
            "Run in Terminal:",
            "sudo /opt/lapso/uninstall.sh"
        );

        // Manual removal instructions
        VerticalLayout manualRemoval = new VerticalLayout();
        manualRemoval.setPadding(true);
        manualRemoval.setSpacing(true);
        manualRemoval.getStyle()
            .set("background", "#ffffff")
            .set("border", "1px solid #fecaca")
            .set("border-radius", "8px")
            .set("margin-top", "12px");

        H4 manualTitle = new H4("Manual Removal (if uninstall script fails):");
        manualTitle.getStyle()
            .set("color", "#991b1b")
            .set("font-weight", "bold")
            .set("margin", "0 0 8px 0");

        Div manualSteps = new Div();
        manualSteps.getElement().setProperty("innerHTML",
            "<ol style='margin:0;padding-left:20px;color:#7f1d1d;line-height:1.8;'>" +
            "<li><strong>Windows:</strong> Stop LAPSO service, delete C:\\Program Files\\LAPSO folder, remove scheduled task</li>" +
            "<li><strong>macOS:</strong> Delete /usr/local/lapso directory, remove launch daemon</li>" +
            "<li><strong>Linux:</strong> Delete /opt/lapso directory, remove systemd service</li>" +
            "<li><strong>All platforms:</strong> Remove device from your dashboard</li>" +
            "</ol>");

        manualRemoval.add(manualTitle, manualSteps);

        Paragraph uninstallNote = new Paragraph("These commands will stop the agent service and remove all installed files. After uninstalling, you can manually delete the device from your dashboard.");
        uninstallNote.getStyle()
            .set("color", "#7f1d1d")
            .set("font-size", "13px")
            .set("margin", "16px 0 0 0")
            .set("font-style", "italic");

        section.add(uninstallTitle, uninstallDesc, winUninstall, macUninstall, linuxUninstall, manualRemoval, uninstallNote);
        return section;
    }

    private Component createUninstallBlock(String platform, String description, String command) {
        VerticalLayout block = new VerticalLayout();
        block.setPadding(true);
        block.setSpacing(false);
        block.getStyle()
            .set("background", "#ffffff")
            .set("border-radius", "8px")
            .set("border", "1px solid #fecaca")
            .set("margin-bottom", "12px");

        H4 platformTitle = new H4(platform);
        platformTitle.getStyle()
            .set("color", "#991b1b")
            .set("font-size", "14px")
            .set("font-weight", "600")
            .set("margin", "0 0 4px 0");

        Paragraph desc = new Paragraph(description);
        desc.getStyle()
            .set("color", "#7f1d1d")
            .set("font-size", "13px")
            .set("margin", "0 0 8px 0");

        Pre commandPre = new Pre(command);
        commandPre.getStyle()
            .set("background", "#450a0a")
            .set("color", "#fecaca")
            .set("padding", "12px")
            .set("border-radius", "6px")
            .set("font-family", "ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace")
            .set("font-size", "13px")
            .set("margin", "0")
            .set("overflow-x", "auto");

        block.add(platformTitle, desc, commandPre);
        return block;
    }

    private String getServerBaseUrl() {
        // Development default. If running behind a reverse proxy, users should replace this with their host.
        return "http://localhost:8080";
    }

    private Component createCodeBlock(String title, String content) {
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setPadding(true);
        wrapper.setSpacing(false);
        wrapper.getStyle()
            .set("background", "#0b1020")
            .set("color", "#e5e7eb")
            .set("border-radius", "8px")
            .set("border", "1px solid #1f2937")
            .set("font-family", "ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace")
            .set("font-size", "13px");

        H4 t = new H4(title);
        t.getStyle().set("color", "#e5e7eb").set("margin", "0 0 6px 0");
        Pre pre = new Pre(content);
        pre.getStyle().set("margin", "0").set("white-space", "pre-wrap");
        wrapper.add(t, pre);
        return wrapper;
    }

    private Component createDownloadCard(String platform, String description, String downloadUrl, String filename) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.getStyle()
            .set("background", "#ffffff")
            .set("border-radius", "12px")
            .set("border", "1px solid #e5e7eb")
            .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)")
            .set("flex", "1")
            .set("text-align", "center")
            .set("cursor", "pointer")
            .set("transition", "all 0.2s ease")
            .set("padding", "24px")
            .set("max-width", "300px");

        card.addClickListener(e -> downloadAgent(platform, downloadUrl, filename));

        H3 titleH3 = new H3(platform);
        titleH3.getStyle()
            .set("color", "#111827")
            .set("font-size", "18px")
            .set("font-weight", "600")
            .set("margin", "0 0 8px 0");

        Paragraph desc = new Paragraph(description);
        desc.getStyle()
            .set("color", "#6b7280")
            .set("font-size", "14px")
            .set("margin", "0 0 16px 0");

        Button downloadBtn = new Button("Download", VaadinIcon.DOWNLOAD.create());
        downloadBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        downloadBtn.getStyle()
            .set("border-radius", "6px")
            .set("font-weight", "500");

        card.add(titleH3, desc, downloadBtn);
        return card;
    }

    private Component createInstructionStep(String number, String title, String description) {
        HorizontalLayout step = new HorizontalLayout();
        step.setAlignItems(FlexComponent.Alignment.CENTER);
        step.setSpacing(true);
        step.getStyle().set("margin-bottom", "0.5rem");

        Span numberSpan = new Span(number);
        numberSpan.getStyle()
            .set("font-size", "1.5rem")
            .set("min-width", "2rem");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);

        H4 titleH4 = new H4(title);
        titleH4.getStyle()
            .set("color", "#1f2937")
            .set("font-weight", "600")
            .set("margin", "0 0 0.25rem 0")
            .set("font-size", "1rem");

        Paragraph desc = new Paragraph(description);
        desc.getStyle()
            .set("color", "#6b7280")
            .set("font-size", "0.875rem")
            .set("margin", "0");

        content.add(titleH4, desc);
        step.add(numberSpan, content);
        return step;
    }

    private void downloadAgent(String platform, String downloadUrl, String filename) {
        // Trigger download using direct link
        UI.getCurrent().getPage().executeJs(
            "window.location.href = $0", downloadUrl
        );

        Notification.show(
            "Downloading LAPSO agent for " + platform + "...", 
            3000, 
            Notification.Position.TOP_CENTER
        ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
