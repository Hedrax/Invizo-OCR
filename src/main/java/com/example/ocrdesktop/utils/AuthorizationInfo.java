package com.example.ocrdesktop.utils;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthorizationInfo {
    public User currentUser;
    public Company company;
    public String accessToken;
    public String refreshToken;

    // We'll make this observable for reactive UI updates
    private final ObjectProperty<Boolean> isAuthenticated = new SimpleObjectProperty<>(false);

    public AuthorizationInfo(User user, Company company, String accessToken, String refreshToken) {
        this.currentUser = user;
        this.company = company;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.isAuthenticated.set(true);
    }


    public void clearAuthentication() {
        this.currentUser = null;
        this.company = null;
        this.accessToken = null;
        this.refreshToken = null;
        this.isAuthenticated.set(false);
    }

    public ObjectProperty<Boolean> isAuthenticated() {
        return isAuthenticated;
    }
}