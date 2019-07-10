package com.example.passwordmanager;

import org.json.JSONArray;

class Directory extends ListableItem {
    JSONArray data;
    public Directory(String type, String name, JSONArray data) {
        super(type, name);
        this.data = data;
    }
}
