package com.example.damka;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Bundle;

public class CustomDialog extends Dialog implements View.OnClickListener {

    private Button btnYes, btnNo;
    private Context context;

    public CustomDialog(Context context) {
        super(context);
        this.context = context;
        setContentView(R.layout.activity_custom_dialog);

        btnYes = findViewById(R.id.btnYes);
        btnNo = findViewById(R.id.btnNo);
        btnYes.setOnClickListener(this);
        btnNo.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        ((GameActivity) context).stopOrStartTimer();
        if (btnYes == view) {
            ((GameActivity) context).resetGame();
            super.dismiss();
        } else if (btnNo == view) {
            ((GameActivity) context).finish();
        }
    }
}