package com.example.myfirstapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create layout programmatically
        ConstraintLayout layout = new ConstraintLayout(this);
        layout.setId(ConstraintLayout.generateViewId());

        // Create TextView
        TextView textView = new TextView(this);
        textView.setId(TextView.generateViewId());
        textView.setText("Hello, Android!");
        textView.setTextSize(24f);

        // Create Button
        Button button = new Button(this);
        button.setId(Button.generateViewId());
        button.setText("Click me!");

        // Change text when button is clicked
        button.setOnClickListener(v -> textView.setText("Button clicked!"));

        // Add views to layout
        layout.addView(textView);
        layout.addView(button);

        // Position views using ConstraintSet
        ConstraintSet set = new ConstraintSet();
        set.clone(layout);

        // TextView constraints
        set.connect(textView.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, 200);
        set.connect(textView.getId(), ConstraintSet.START, layout.getId(), ConstraintSet.START, 50);
        set.connect(textView.getId(), ConstraintSet.END, layout.getId(), ConstraintSet.END, 50);

        // Button constraints
        set.connect(button.getId(), ConstraintSet.TOP, textView.getId(), ConstraintSet.BOTTOM, 50);
        set.connect(button.getId(), ConstraintSet.START, layout.getId(), ConstraintSet.START, 50);
        set.connect(button.getId(), ConstraintSet.END, layout.getId(), ConstraintSet.END, 50);

        set.applyTo(layout);

        setContentView(layout);
    }
}
