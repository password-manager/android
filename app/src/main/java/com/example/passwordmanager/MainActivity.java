package com.example.passwordmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends Activity  implements AdapterView.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{
    Button newDirectoryButton, newPasswordButton, logoutButton, higherDirectory;
    String currentPath, masterPassword;
    CheckBox showPassword;
    JSONArray database;
    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while (true) {
                if (!((line = reader.readLine()) != null)) break;
                sb.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public JSONArray getDirectory(String path) throws JSONException {
        if (path.equals("/")) return database;
        String[] pathArray = Arrays.copyOfRange(path.split("/"), 1, path.split("/").length);
        JSONArray directory = database;
        for (String directoryName : pathArray){
            for (int i = 0; i < directory.length(); i++){
                JSONObject item = directory.getJSONObject(i);
                if (item.optString("type").equals("directory") && item.optString("name").equals(directoryName)){
                    directory = item.getJSONArray("data");
                    break;
                }
            }
        }
        return directory;
    }

    @Override
    public void onClick(View v) {
        if (currentPath.equals("/")){
            return;
        }else{
            try {
                String[] pathArray = Arrays.copyOfRange(currentPath.split("/"), 0, currentPath.split("/").length - 1);
                currentPath = String.join("/", pathArray);
                JSONArray subdirectory = getDirectory(currentPath);
                arrayOfItems.clear();
                populateList(arrayOfItems, subdirectory);
                ListView listView = (ListView) findViewById(R.id.main_passwordsList);
                ListableItemsAdapter adapter = new ListableItemsAdapter(getApplicationContext(), arrayOfItems);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        Intent intent = new Intent();
        String name = ((TextView) v.findViewById(R.id.name)).getText().toString();
        String type = ((TextView) v.findViewById(R.id.type)).getText().toString();
        if (type.equals("password")){
            //display password
        } else {
            //change directory
            try {
                currentPath = currentPath + name + "/";
                JSONArray subdirectory = getDirectory(currentPath);
                arrayOfItems.clear();
                populateList(arrayOfItems, subdirectory);
                ListView listView = (ListView) findViewById(R.id.main_passwordsList);
                ListableItemsAdapter adapter = new ListableItemsAdapter(this, arrayOfItems);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //startActivity(intent);
    }

    public boolean onItemLongClick(AdapterView<?> arg0, View v,
                                   int position, long id) {
        Intent intent = new Intent();
        String name = ((TextView) v.findViewById(R.id.name)).getText().toString();
        String type = ((TextView) v.findViewById(R.id.type)).getText().toString();
        intent.putExtra("position", position);
        intent.putExtra("id", id);
        intent.putExtra("path", currentPath);
        intent.putExtra("name", name);
        intent.putExtra("database", database.toString());
        intent.putExtra("operation_type", "edit");
        if (type.equals("password")){
            //edit password
            intent.setClass(this, EditPasswordActivity.class);
        } else {
            //edit directory
            intent.setClass(this, EditDirectoryActivity.class);
        }
        startActivity(intent);
        return true;
    }


    public void sendMessage(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    ArrayList<ListableItem> arrayOfItems = new ArrayList<ListableItem>();
    String file = "filename";
    private void populateList(List<ListableItem> arrayOfItems, JSONArray jsonMainNode) throws JSONException {
        for(int i = 0; i<jsonMainNode.length();i++){
            JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
            if (jsonChildNode.optString("type").equals("directory")){
                String type = "directory";
                String name = jsonChildNode.optString("name");
                JSONArray data = jsonChildNode.getJSONArray("data");
                arrayOfItems.add(new Directory(type, name, data));
            } else if (jsonChildNode.optString("type").equals("password")){
                String type = "password";
                String name = jsonChildNode.optString("name");
                String data = jsonChildNode.optString("data");
                arrayOfItems.add(new Password(type, name, data));
            }
        }
    }

    private void initList(){

        try{
            String file = "filename";
            FileInputStream fin = openFileInput(file);
            String temp = convertStreamToString(fin);
            database = new JSONArray(temp);
            fin.close();
            populateList(arrayOfItems, database);
            ListView listView = (ListView) findViewById(R.id.main_passwordsList);
            ListableItemsAdapter adapter = new ListableItemsAdapter(this, arrayOfItems);
            //SimpleAdapter simpleAdapter = new SimpleAdapter(this, employeeList, android.R.layout.simple_list_item_1, new String[] {"employees"}, new int[] {android.R.id.text1});
            listView.setAdapter(adapter);
            //ListView listview = (ListView) findViewById(R.id.listView1);
            listView.setOnItemClickListener(this);
            listView.setOnItemLongClickListener(this);
        }
        catch(JSONException e){
            Toast.makeText(getApplicationContext(), "Error"+e.toString(), Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, String> createEmployee(String name, String number){
        HashMap<String, String> employeeNameNo = new HashMap<String, String>();
        employeeNameNo.put(name, number);
        return employeeNameNo;
    }

    @Override
    public void onResume(){
        super.onResume();
        try {
            String file = "filename";
            FileInputStream fin = openFileInput(file);
            String temp = convertStreamToString(fin);
            database = new JSONArray(temp);
            fin.close();
            JSONArray subdirectory = getDirectory(currentPath);
            arrayOfItems.clear();
            populateList(arrayOfItems, subdirectory);
            ListView listView = (ListView) findViewById(R.id.main_passwordsList);
            ListableItemsAdapter adapter = new ListableItemsAdapter(this, arrayOfItems);
            //SimpleAdapter simpleAdapter = new SimpleAdapter(this, employeeList, android.R.layout.simple_list_item_1, new String[] {"employees"}, new int[] {android.R.id.text1});
            listView.setAdapter(adapter);
            //ListView listview = (ListView) findViewById(R.id.listView1);
            listView.setOnItemClickListener(this);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        initList();

        newDirectoryButton = (Button)findViewById(R.id.main_addDirectoryButton);
        newPasswordButton = (Button)findViewById(R.id.main_addPasswordButton);
        logoutButton = (Button)findViewById(R.id.main_logoutButton);
        higherDirectory = (Button)findViewById(R.id.main_exitDirectoryButton);
        currentPath = "/";
        Bundle bundle = getIntent().getExtras();
        masterPassword = bundle.getString("master-password");


        newDirectoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("operation_type", "new");
                intent.putExtra("path", currentPath);
                intent.putExtra("database", database.toString());
                intent.setClass(getApplicationContext(), EditDirectoryActivity.class);
                startActivity(intent);
            }
        });


        newPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("operation_type", "new");
                intent.putExtra("path", currentPath);
                intent.putExtra("database", database.toString());
                intent.setClass(getApplicationContext(), EditPasswordActivity.class);
                startActivity(intent);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        higherDirectory.setOnClickListener(this);
    }
}