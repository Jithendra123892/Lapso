package com.example.demo.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("")
@AnonymousAllowed
public class RootView extends VerticalLayout implements BeforeEnterObserver {

    public RootView() {
        // Show loading message while redirecting
        add(new H1("LAPSO"));
        add(new Paragraph("Loading..."));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Redirect to dashboard instead of login page
        event.forwardTo("dashboard");
    }
}