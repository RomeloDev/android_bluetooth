package com.example.bluetoothapprp;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DeviceAdapter extends ArrayAdapter<String> {

    public DeviceAdapter(Context context, ArrayList<String> devices) {
        super(context, 0, devices);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String device = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        }
        TextView text1 = convertView.findViewById(android.R.id.text1);
        TextView text2 = convertView.findViewById(android.R.id.text2);
        text1.setText(device.split("\n")[0]); // Device name
        text2.setText(device.split("\n")[1]); // Device address
        return convertView;
    }
}