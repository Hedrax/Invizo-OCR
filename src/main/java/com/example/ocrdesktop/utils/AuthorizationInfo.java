package com.example.ocrdesktop.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

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

    //Saved AuthorizationInfo is only name of the user and organization in the portable version
    public AuthorizationInfo(String username, String organizationName) {
        this.currentUser = new User(UUID.randomUUID().toString(), username, "", User.Role.ROLE_COMPANY_ADMIN);
        this.company = new Company(UUID.randomUUID().toString(), organizationName);
        this.accessToken = "";
        this.refreshToken = "";
    }


    public void clearAuthentication() {
        this.currentUser = null;
        this.company = null;
        this.accessToken = null;
        this.refreshToken = null;
    }

}