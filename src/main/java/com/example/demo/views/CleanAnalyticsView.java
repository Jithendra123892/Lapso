package com.example.demo.views;

import com.example.demo.service.PerfectAuthService;
import com.example.demo.service.AnalyticsService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Route("analytics")
@PageTitle("Security Analytics - LAPSO Professional")
@AnonymousAllowed
public class CleanAnalyticsView extends VerticalLayout {

    private final PerfectAuthService authService;
    private final AnalyticsService analyticsService;

    public CleanAnalyticsView(PerfectAuthService authService, AnalyticsService analyticsService) {
        this.authService = authService;
        this.analyticsService = analyticsService;
        
        // Removed authentication check to allow access without login
        System.out.println("Showing analytics interface without authentication check");
        createCleanAnalyticsInterface();
    }

    private void createCleanAnalyticsInterface() {
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
            .set("max-width", "1200px")
            .set("margin", "0 auto")
            .set("width", "100%");
        
        // Overview cards
        mainContent.add(createOverviewCards());
        
        // Charts section
        mainContent.add(createChartsSection());
        
        // Activity timeline
        mainContent.add(createActivityTimeline());
        
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
        
        Button backBtn = new Button("← Dashboard", VaadinIcon.ARROW_LEFT.create());
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backBtn.addClickListener(e -> UI.getCurrent().navigate(""));
        
        H1 title = new H1("📊 Security Analytics & Intelligence");
        title.getStyle()
            .set("color", "#1f2937")
            .set("font-size", "1.5rem")
            .set("font-weight", "700")
            .set("margin", "0");
        
        leftSection.add(backBtn, title);

        // Time range selector
        HorizontalLayout timeRange = new HorizontalLayout();
        timeRange.setAlignItems(FlexComponent.Alignment.CENTER);
        timeRange.setSpacing(true);
        
        Button last7Days = new Button("7 Days");
        last7Days.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        
        Button last30Days = new Button("30 Days");
        last30Days.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        
        Button last90Days = new Button("90 Days");
        last90Days.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        
        timeRange.add(last7Days, last30Days, last90Days);
        
        header.add(leftSection, timeRange);
        return header;
    }

    private Component createOverviewCards() {
        HorizontalLayout overviewCards = new HorizontalLayout();
        overviewCards.setWidthFull();
        overviewCards.setSpacing(true);
        overviewCards.getStyle()
            .set("margin-bottom", "2rem");

        // Use a default email when not authenticated
        String userEmail = "guest@example.com";
        Map<String, Object> analytics = analyticsService.getDashboardAnalytics(userEmail != null ? userEmail : "");

        overviewCards.add(
            createOverviewCard("📈", "Total Tracking Hours", "168.5h", "+12% vs last week", "#10b981"),
            createOverviewCard("🎯", "Location Accuracy", "98.7%", "+0.3% improvement", "#3b82f6"),
            createOverviewCard("⚡", "Response Time", "1.2s", "-0.3s faster", "#8b5cf6"),
            createOverviewCard("🔒", "Security Score", "95/100", "+5 points", "#f59e0b")
        );

        return overviewCards;
    }

    private Component createOverviewCard(String icon, String title, String value, String change, String color) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
            .set("background", "#ffffff")
            .set("border-radius", "1rem")
            .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)")
            .set("border", "1px solid #e5e7eb")
            .set("flex", "1")
            .set("min-width", "200px")
            .set("transition", "all 0.2s ease");

        // Hover effect
        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle().set("box-shadow", "0 4px 12px rgba(0, 0, 0, 0.15)");
        });
        
        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle().set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)");
        });

        // Header with icon
        HorizontalLayout cardHeader = new HorizontalLayout();
        cardHeader.setWidthFull();
        cardHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        cardHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Span iconSpan = new Span(icon);
        iconSpan.getStyle().set("font-size", "1.5rem");

        Span trendIcon = new Span("📈");
        trendIcon.getStyle()
            .set("font-size", "1rem")
            .set("color", color);

        cardHeader.add(iconSpan, trendIcon);

        // Value
        H2 valueH2 = new H2(value);
        valueH2.getStyle()
            .set("color", color)
            .set("font-size", "2rem")
            .set("font-weight", "800")
            .set("margin", "0.5rem 0 0.25rem 0");

        // Title
        H4 titleH4 = new H4(title);
        titleH4.getStyle()
            .set("color", "#1f2937")
            .set("font-size", "0.875rem")
            .set("font-weight", "600")
            .set("margin", "0 0 0.5rem 0");

        // Change indicator
        Span changeSpan = new Span(change);
        changeSpan.getStyle()
            .set("color", "#10b981")
            .set("font-size", "0.75rem")
            .set("font-weight", "500");

        card.add(cardHeader, valueH2, titleH4, changeSpan);
        return card;
    }

    private Component createChartsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);

        H2 sectionTitle = new H2("Performance Charts");
        sectionTitle.getStyle()
            .set("color", "#1f2937")
            .set("font-size", "1.5rem")
            .set("font-weight", "700")
            .set("margin", "0 0 1rem 0");

        // Charts grid
        HorizontalLayout chartsGrid = new HorizontalLayout();
        chartsGrid.setWidthFull();
        chartsGrid.setSpacing(true);

        chartsGrid.add(
            createChartCard("📊", "Device Activity", "Track device usage patterns over time"),
            createChartCard("🌍", "Location History", "Visualize movement patterns and hotspots")
        );

        section.add(sectionTitle, chartsGrid);
        return section;
    }

    private Component createChartCard(String icon, String title, String description) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle()
            .set("background", "#ffffff")
            .set("border-radius", "1rem")
            .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)")
            .set("border", "1px solid #e5e7eb")
            .set("flex", "1")
            .set("min-height", "300px");

        // Chart header
        HorizontalLayout chartHeader = new HorizontalLayout();
        chartHeader.setWidthFull();
        chartHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        chartHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        HorizontalLayout titleSection = new HorizontalLayout();
        titleSection.setAlignItems(FlexComponent.Alignment.CENTER);
        titleSection.setSpacing(true);

        Span iconSpan = new Span(icon);
        iconSpan.getStyle().set("font-size", "1.5rem");

        H3 chartTitle = new H3(title);
        chartTitle.getStyle()
            .set("color", "#1f2937")
            .set("font-size", "1.125rem")
            .set("font-weight", "600")
            .set("margin", "0");

        titleSection.add(iconSpan, chartTitle);

        Button exportBtn = new Button("Export", VaadinIcon.DOWNLOAD.create());
        exportBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        chartHeader.add(titleSection, exportBtn);

        // Chart placeholder
        VerticalLayout chartPlaceholder = new VerticalLayout();
        chartPlaceholder.setSizeFull();
        chartPlaceholder.setAlignItems(FlexComponent.Alignment.CENTER);
        chartPlaceholder.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        chartPlaceholder.getStyle()
            .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
            .set("border-radius", "0.75rem")
            .set("color", "#ffffff")
            .set("text-align", "center")
            .set("min-height", "200px");

        Span chartIcon = new Span("📈");
        chartIcon.getStyle().set("font-size", "3rem");

        Paragraph chartDesc = new Paragraph(description);
        chartDesc.getStyle()
            .set("color", "rgba(255, 255, 255, 0.9)")
            .set("margin", "1rem 0 0 0");

        chartPlaceholder.add(chartIcon, chartDesc);

        card.add(chartHeader, chartPlaceholder);
        return card;
    }

    private Component createActivityTimeline() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.setSpacing(true);
        section.getStyle()
            .set("background", "#ffffff")
            .set("border-radius", "1rem")
            .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)")
            .set("border", "1px solid #e5e7eb");

        // Section header
        HorizontalLayout sectionHeader = new HorizontalLayout();
        sectionHeader.setWidthFull();
        sectionHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        sectionHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H2 sectionTitle = new H2("📅 Recent Activity");
        sectionTitle.getStyle()
            .set("color", "#1f2937")
            .set("font-size", "1.5rem")
            .set("font-weight", "700")
            .set("margin", "0");

        Button viewAllBtn = new Button("View All", VaadinIcon.EXTERNAL_LINK.create());
        viewAllBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        sectionHeader.add(sectionTitle, viewAllBtn);

        // Timeline items
        VerticalLayout timeline = new VerticalLayout();
        timeline.setPadding(false);
        timeline.setSpacing(true);

        timeline.add(
            createTimelineItem("🟢", "Device Online", "MacBook Pro connected from New York", "2 minutes ago", "#10b981"),
            createTimelineItem("📍", "Location Update", "iPhone location updated to San Francisco", "15 minutes ago", "#3b82f6"),
            createTimelineItem("🔒", "Security Scan", "All devices passed security check", "1 hour ago", "#8b5cf6"),
            createTimelineItem("📊", "Report Generated", "Weekly analytics report created", "3 hours ago", "#f59e0b"),
            createTimelineItem("🚨", "Alert Resolved", "Suspicious activity alert cleared", "1 day ago", "#ef4444")
        );

        section.add(sectionHeader, timeline);
        return section;
    }

    private Component createTimelineItem(String icon, String title, String description, String time, String color) {
        HorizontalLayout item = new HorizontalLayout();
        item.setWidthFull();
        item.setAlignItems(FlexComponent.Alignment.CENTER);
        item.setSpacing(true);
        item.setPadding(true);
        item.getStyle()
            .set("border-radius", "0.5rem")
            .set("transition", "all 0.2s ease");

        // Hover effect
        item.getElement().addEventListener("mouseenter", e -> {
            item.getStyle().set("background", "#f8fafc");
        });
        
        item.getElement().addEventListener("mouseleave", e -> {
            item.getStyle().set("background", "transparent");
        });

        // Icon
        Span iconSpan = new Span(icon);
        iconSpan.getStyle()
            .set("font-size", "1.5rem")
            .set("min-width", "2rem");

        // Content
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.getStyle().set("flex", "1");

        H4 itemTitle = new H4(title);
        itemTitle.getStyle()
            .set("color", "#1f2937")
            .set("font-size", "1rem")
            .set("font-weight", "600")
            .set("margin", "0 0 0.25rem 0");

        Paragraph itemDesc = new Paragraph(description);
        itemDesc.getStyle()
            .set("color", "#6b7280")
            .set("font-size", "0.875rem")
            .set("margin", "0");

        content.add(itemTitle, itemDesc);

        // Time
        Span timeSpan = new Span(time);
        timeSpan.getStyle()
            .set("color", "#9ca3af")
            .set("font-size", "0.75rem")
            .set("font-weight", "500")
            .set("min-width", "100px")
            .set("text-align", "right");

        item.add(iconSpan, content, timeSpan);
        return item;
    }
}
