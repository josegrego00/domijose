package com.josepinodev.appdomirest.dto;

import org.springframework.context.ApplicationEvent;

public class DashboardUpdateEvent extends ApplicationEvent {

    public DashboardUpdateEvent(Object source) {
        super(source);
    }
}
