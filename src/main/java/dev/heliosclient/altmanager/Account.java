package dev.heliosclient.altmanager;

import dev.heliosclient.altmanager.accounts.CrackedAccount;

public abstract class Account {
    private String username;
    private String password;
    //Kindof to be used while saving. Idk
    private boolean wasPreviouslyLoggedIn;

    public Account(String username, String password, boolean wasPreviouslyLoggedIn) {
        this.username = username;
        this.password = password;
        this.wasPreviouslyLoggedIn = wasPreviouslyLoggedIn;
    }
    public Account(String username, String password) {
        this.username = username;
        this.password = password;
        this.wasPreviouslyLoggedIn = false;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isCracked(){
        return this instanceof CrackedAccount || (password == null && !username.isEmpty());
    }

    public abstract boolean login();
    public abstract String getDisplayName();

    @Override
    public String toString() {
        return getDisplayName();
    }
}
