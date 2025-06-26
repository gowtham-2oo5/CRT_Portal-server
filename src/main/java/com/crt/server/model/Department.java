package com.crt.server.model;

public enum Department {
    CSE("Computer Science & Engineering"),
    ME("Mechanical Engineering"),
    CE("Civil Engineering"),
    ECE("Electronics & Communication Engineering"),
    EEE("Electrical & Electronics Engineering");

    private final String displayName;

    Department(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
