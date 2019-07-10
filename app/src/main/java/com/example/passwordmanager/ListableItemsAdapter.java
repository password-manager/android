package com.example.passwordmanager;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListableItemsAdapter extends ArrayAdapter<ListableItem> {
    public ListableItemsAdapter(Context context, ArrayList<ListableItem> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ListableItem item = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_listitem, parent, false);
        }
        // Lookup view for data population
        //TextView tvName = (TextView) convertView.findViewById(R.id.my_textview);
        TextView tvName = (TextView) convertView.findViewById(R.id.name);
        ImageView ivIcon = (ImageView) convertView.findViewById(R.id.imageView1);
        // Populate the data into the template view using the data object
        //tvName.setText(user.name);
        tvName.setText(item.name);
        Log.i("LitableItemType", item.type);
        if (item.type.equals("directory")) {
            ivIcon.setImageResource(R.drawable.ic_directory);
        } else if (item.type.equals("password")){
            ivIcon.setImageResource(R.drawable.ic_password);
        }
        // Return the completed view to render on screen
        return convertView;
    }
}