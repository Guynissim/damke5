package com.example.damka;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

public class CustomDialog extends Dialog implements View.OnClickListener {

    private Button noButton, yesButton;
    public CustomDialog(Context context) {
        super(context);
        setContentView(R.layout.activity_custom_dialog);

        noButton = findViewById(R.id.noButton);
        yesButton = findViewById(R.id.yesButton);
        yesButton.setOnClickListener(this);
        noButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (noButton == view) {

        }
        if (yesButton == view) {

        }
    }
}