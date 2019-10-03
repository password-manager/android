package com.example.passwordmanager;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static java.lang.Thread.sleep;

public class Userbase {
    List<String> records = new ArrayList<>();
    Context ctx;
    String fileName = "users";
    public Userbase(Context ctx) {
        this.ctx = ctx;
        FileInputStream fIn = null;
        String fileContents = "";
        try {
            byte[] buffer = new byte[1000];
            fIn = ctx.openFileInput(fileName);
            while (fIn.read(buffer) != -1) {
                fileContents += new String(buffer);
            }
            fIn.close();
            String[] recordsArray = fileContents.split("\\n");
            records = new ArrayList<>(Arrays.asList(recordsArray));
        } catch (FileNotFoundException e) {
            try {
                FileOutputStream fOut = ctx.openFileOutput(fileName, MODE_PRIVATE);
                fOut.write(new byte[]{});
                fOut.flush();
                fOut.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User getUser(String email){
        for(String record : records){
            if (record.split(":").length > 1) {
                User user = new User(record);
                if (user.getEmail().equals(email))
                    return user;
            }
        }
        return null;
    }

    public void saveUser(User user){
        records.add(user.toRecord());
        this.writeToFile();
    }

    private void writeToFile(){
        String fileName = "users";
        FileOutputStream fOut = null;
        String fileString = "";
        for (String record : records)
            fileString += record + "\n";
        try {
            fOut = ctx.openFileOutput(fileName, MODE_PRIVATE);
            fOut.write(fileString.getBytes());
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
