package com.example.passwordmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ListableItem {
    public int i = 1;
    public String type;
    public String name;

    public ListableItem(String type, String name) {
        this.type = type;
        this.name = name;
    }

}