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


    public AuthorizationInfo(User user, Company company, String accessToken, String refreshToken) {
        this.currentUser = user;
        this.company = company;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }


    public void clearAuthentication() {
        this.currentUser = null;
        this.company = null;
        this.accessToken = null;
        this.refreshToken = null;
    }

}