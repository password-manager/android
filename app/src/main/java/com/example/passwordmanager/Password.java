package com.example.passwordmanager;

class Password extends ListableItem {
    String data;
    public Password(String type, String name, String data) {
        super(type, name);
        this.data = data;
    }
}
