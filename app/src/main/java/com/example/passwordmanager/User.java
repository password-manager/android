package com.example.passwordmanager;

import android.util.Base64;
import android.util.Log;

public class User {
    private String email;
    private byte[] passwordHash;
    private byte[] salt;
    String path = "";

    public User(String email, byte[] passwordHash, byte[] salt) {
        this.email = email;
        this.salt = salt;
        this.passwordHash = passwordHash;
    }

    public User(String email, byte[] passwordHash, byte[] salt, String path) {
        this.email = email;
        this.salt = salt;
        this.passwordHash = passwordHash;
        this.path = path;
    }

    public User(String record) {
        String[] userData = record.split(":");
        this.email = userData[0];
        this.passwordHash = Base64.decode(userData[1], 0);
        this.salt = Base64.decode(userData[2], 0);
        if (userData.length > 3) this.path = userData[3];
    }

    public String toRecord(){
        return this.email + ":" + Base64.encodeToString(passwordHash, 0).replaceAll("\n", "") + ":" + Base64.encodeToString(salt, 0).replaceAll("\n", "") + ":" + this.path;
    }

    public String getEmail() {
        return email;
    }

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getPasswordHash() {
        return passwordHash;
    }

    public String getPath() {
        return path;
    }
}
