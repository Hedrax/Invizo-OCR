package com.example.ocrdesktop.utils;

public class AuthorizationInfo {
    public User currentUser;
    public Organization organization;
    public AuthorizationInfo(User user, Organization organization){
        this.currentUser = user;
        this.organization = organization;
    }

}
