package com.example.ocrdesktop.utils;

public class User {
    // 2. add a refresh by repo.getAllUsers()

    public String id;
    public String userName;
    public String email;
    public Role role;
    public User (String id, String userName,String email, Role role){
        this.id =id;
        this.userName = userName;
        this.email = email;
        this.role = role;
    }
    public enum Role{
        ROLE_COMPANY_ADMIN,
        ROLE_DESKTOP_USER,
        ROLE_MOBILE_USER
    }
}
