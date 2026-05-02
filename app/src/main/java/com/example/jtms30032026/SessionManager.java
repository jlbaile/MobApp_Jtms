package com.example.jtms30032026;

/**
 * SessionManager — simple singleton that holds the current logged-in user's
 * username and role for the lifetime of the app process.
 *
 * Set once in LoginFragment after a successful login.
 * Read anywhere in the app (Fragments, Adapters) via SessionManager.getInstance().
 */
public class SessionManager {

    private static SessionManager instance;

    private String loggedInUsername = "";
    private boolean isAdmin         = false;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setLoggedInUsername(String username) {
        this.loggedInUsername = username;
    }

    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getLoggedInUsername() {
        return loggedInUsername;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    // ── Clear session on logout ───────────────────────────────────────────────

    public void clear() {
        loggedInUsername = "";
        isAdmin          = false;
    }
}