package com.example.demo.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Simple error view to handle routing issues
 */
@PageTitle("LAPSO - Error")
@AnonymousAllowed
public class ErrorView extends VerticalLayout implements HasErrorParameter<Exception> {

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<Exception> parameter) {
        createErrorView(parameter.getException());
        return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }

    private void createErrorView(Exception exception) {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        
        getStyle()
            .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
            .set("color", "white")
            .set("font-family", "'Inter', 'Segoe UI', system-ui, sans-serif");

        VerticalLayout errorCard = new VerticalLayout();
        errorCard.setPadding(true);
        errorCard.setSpacing(true);
        errorCard.setAlignItems(FlexComponent.Alignment.CENTER);
        errorCard.setWidth("500px");
        errorCard.getStyle()
            .set("background", "rgba(255, 255, 255, 0.1)")
            .set("backdrop-filter", "blur(20px)")
            .set("border", "1px solid rgba(255, 255, 255, 0.2)")
            .set("border-radius", "24px")
            .set("padding", "40px")
            .set("text-align", "center");

        Span icon = new Span("🔒");
        icon.getStyle().set("font-size", "60px");

        H1 title = new H1("Access Error");
        title.getStyle()
            .set("color", "white")
            .set("margin", "20px 0 10px 0")
            .set("font-size", "2rem");

        Paragraph message = new Paragraph("An error occurred while accessing this page");
        message.getStyle()
            .set("color", "rgba(255,255,255,0.9)")
            .set("margin", "0 0 30px 0")
            .set("font-size", "1.1rem");

        Button dashboardBtn = new Button("Go to Dashboard", VaadinIcon.ARROW_RIGHT.create());
        dashboardBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        dashboardBtn.getStyle()
            .set("background", "rgba(255,255,255,0.2)")
            .set("border", "2px solid rgba(255,255,255,0.3)")
            .set("border-radius", "12px")
            .set("color", "white")
            .set("font-weight", "600");
        dashboardBtn.addClickListener(e -> UI.getCurrent().navigate("dashboard"));

        errorCard.add(icon, title, message, dashboardBtn);
        add(errorCard);
    }
}