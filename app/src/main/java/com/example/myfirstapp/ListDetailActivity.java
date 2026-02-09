package com.example.myfirstapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ListDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_details);

        TextView titleView = findViewById(R.id.listTitle);

        // Get the list title passed from ListAdapter
        String title = getIntent().getStringExtra("list_title");

        if (title != null) {
            titleView.setText(title);
        }
    }
}
