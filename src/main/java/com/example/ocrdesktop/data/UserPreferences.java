package com.example.ocrdesktop.data;
import com.example.ocrdesktop.utils.AuthorizationInfo;
import com.example.ocrdesktop.utils.Company;
import com.example.ocrdesktop.utils.User;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class UserPreferences {

    // Constants for keys
    private static final String ACCESS_TOKEN_KEY = "accessToken";
    private static final String REFRESH_TOKEN_KEY = "refreshToken";
    private static final String USER_ID_KEY = "userId";
    private static final String USERNAME_KEY = "username";
    private static final String EMAIL_KEY = "email";
    private static final String USER_ROLE_KEY = "role";
    private static final String COMPANY_ID_KEY = "companyId";
    private static final String COMPANY_NAME_KEY = "company";
    private static final String LOGGED_IN_KEY = "loggedIn";

    // Preferences object
    private static final Preferences prefs = Preferences.userRoot().node("myApp");

    // Save credentials
    public static void saveCredentials(AuthorizationInfo authorizationInfo) throws BackingStoreException {
        //first saving tokens

        prefs.put(ACCESS_TOKEN_KEY, authorizationInfo.accessToken);
        prefs.put(REFRESH_TOKEN_KEY, authorizationInfo.refreshToken);

        if(authorizationInfo.currentUser != null) {
            prefs.put(USER_ID_KEY, authorizationInfo.currentUser.id);
            prefs.put(USERNAME_KEY, authorizationInfo.currentUser.userName);
            prefs.put(EMAIL_KEY, authorizationInfo.currentUser.email);
            prefs.put(USER_ROLE_KEY, authorizationInfo.currentUser.role.toString());

            prefs.put(COMPANY_ID_KEY, authorizationInfo.company.companyId);
            prefs.put(COMPANY_NAME_KEY, authorizationInfo.company.name);
        }

        prefs.putBoolean(LOGGED_IN_KEY, true);
        prefs.flush(); // Ensure data is written immediately
    }

    // Check if user is already logged in
    public static boolean isLoggedIn() {
        return prefs.getBoolean(LOGGED_IN_KEY, false);
    }

    // Retrieve saved username
    public static AuthorizationInfo getCredentials() {
        return new AuthorizationInfo(
                new User(prefs.get(USER_ID_KEY, null), prefs.get(USERNAME_KEY, null), prefs.get(EMAIL_KEY, null), User.Role.ROLE_COMPANY_ADMIN),
                new Company(prefs.get(COMPANY_ID_KEY,null), prefs.get(COMPANY_NAME_KEY, null)),
                prefs.get(ACCESS_TOKEN_KEY,null),
                prefs.get(REFRESH_TOKEN_KEY, null)
        );
    }

    // Clear user preferences (Logout)
    public static void clearCredentials() throws BackingStoreException {

        prefs.remove(ACCESS_TOKEN_KEY);
        prefs.remove(REFRESH_TOKEN_KEY);

        prefs.remove(USER_ID_KEY);
        prefs.remove(USERNAME_KEY);
        prefs.remove(EMAIL_KEY);

        prefs.remove(COMPANY_ID_KEY);
        prefs.remove(COMPANY_NAME_KEY);
        prefs.remove(USER_ROLE_KEY);


        prefs.putBoolean(LOGGED_IN_KEY, false);
        prefs.flush();
    }
}
