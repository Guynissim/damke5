package com.example.damka;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

public class CustomDialog extends Dialog implements View.OnClickListener {



    private Button noButton, yesButton;
    private GameActivity gameActivity;

    public CustomDialog(GameActivity activity) {
        super(activity);
        this.gameActivity = activity; // Store the reference
        setContentView(R.layout.activity_custom_dialog);

        noButton = findViewById(R.id.noButton);
        noButton.setOnClickListener(this);
        yesButton = findViewById(R.id.yesButton);
        yesButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (noButton == view) {
            this.dismiss();
        }
        if (yesButton == view) {
            gameActivity.quitGame();
            dismiss();
        }
    }
}