package com.example.passwordmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends Activity  implements AdapterView.OnItemClickListener {
    Button b1,b2,b3;
    String currentPath;
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

    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        Intent intent = new Intent();
        intent.setClass(this, ListableItem.class);
        intent.putExtra("position", position);
        intent.putExtra("id", id);
        intent.putExtra("path", currentPath + "/" +v.findViewById(R.id.name));
        //startActivity(intent);
    }

    private String jsonTest = "[\n" +
            "\t{\n" +
            "\t\"type\": \"password\",\n" +
            "\t\"name\": \"moje pijrwsze hasło\",\n" +
            "\t\"data\": \"haslo1234\"\n" +
            "\t},\n" +
            "\t{\n" +
            "\t\"type\": \"directory\",\n" +
            "\t\"name\": \"mój pierwszy katalog\",\n" +
            "\t\"data\":[\n" +
            "\t\t\t{\n" +
            "\t\t\t\"type\": \"password\",\n" +
            "\t\t\t\"name\": \"hasło do pentagonu\",\n" +
            "\t\t\t\"data\": \"asdffdsa@\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\"type\": \"password\",\n" +
            "\t\t\t\"name\": \"hasło do konta bankowego\",\n" +
            "\t\t\t\"data\": \"a1!@#$%^&*\\\\\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\"type\": \"directory\",\n" +
            "\t\t\t\"name\": \"zagnieżdżony, pusty katlog\",\n" +
            "\t\t\t\"data\": []\n" +
            "\t\t\t}\n" +
            "\t\t]\n" +
            "\t}\n" +
            "]";

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
            FileOutputStream fOut = openFileOutput(file, MODE_PRIVATE);
            fOut.write(jsonTest.getBytes());
            FileInputStream fin = openFileInput(file);
            String temp = convertStreamToString(fin);
            JSONArray jsonMainNode = new JSONArray(temp);
            fin.close();
            populateList(arrayOfItems, jsonMainNode);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        initList();
        ListView listView = (ListView) findViewById(R.id.listView1);
        ListableItemsAdapter adapter = new ListableItemsAdapter(this, arrayOfItems);
        //SimpleAdapter simpleAdapter = new SimpleAdapter(this, employeeList, android.R.layout.simple_list_item_1, new String[] {"employees"}, new int[] {android.R.id.text1});
        listView.setAdapter(adapter);
        //ListView listview = (ListView) findViewById(R.id.listView1);
        listView.setOnItemClickListener(this);

        b1 = (Button)findViewById(R.id.button1);
        b2 = (Button)findViewById(R.id.button2);
        b3 = (Button)findViewById(R.id.button3);

        currentPath = "/";
        //tx1 = (TextView)findViewById(R.id.textView3);
        //tx1.setVisibility(View.GONE);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(v);
                /*if(ed1.getText().toString().equals("admin") &&
                        ed2.getText().toString().equals("admin")) {
                    Toast.makeText(getApplicationContext(),
                            "Redirecting...",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Wrong Credentials", Toast.LENGTH_SHORT).show();

                            //tx1.setVisibility(View.VISIBLE);
                    //tx1.setBackgroundColor(Color.RED);
                    counter--;
                    //tx1.setText(Integer.toString(counter));

                    if (counter == 0) {
                        b1.setEnabled(false);
                    }
                }*/
            }
        });


        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}