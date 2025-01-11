package com.example.ocrdesktop.utils;

public class User {
    // 2. add a refresh by repo.getAllUsers()

    public String id;
    public String userName;
    public String email;
    public Role role;
    private String password = PASSWORD_DEFAULT;
    public User (String id, String userName,String email, Role role){
        this.id =id;
        this.userName = userName;
        this.email = email;
        this.role = role;
    }
    public enum Role{
        SUPER_ADMIN,
        DESKTOP_USER,
        MOBILE_USER
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getPassword() {
        return password;
    }
    @Override
    public boolean equals(Object obj) {
        // Check if the same reference
        if (this == obj) return true;
        // Check for null or different class
        if (obj == null || getClass() != obj.getClass()) return false;

        // Cast and compare based on 'id'
        User user = (User) obj;
        return this.id.equals(user.id) &&
                this.userName.equals(user.userName) &&
                this.email.equals(user.email) &&
                this.role == user.role && this.password.equals(user.password);
    }
    public static String PASSWORD_DEFAULT = "********";
}
