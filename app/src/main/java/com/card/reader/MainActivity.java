package com.card.reader;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {


    CountDownTimer timer;
    ProgressBar progressbar;
    int progress = 0;
    int timeLoad = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressbar = findViewById(R.id.barra_progreso);
        progressbar.setProgress(progress);

        progressbar.setMax(timeLoad  / 1000);


        timer = new CountDownTimer(timeLoad , 1000) {
            public void onFinish() {
                closeScreen();
            }
            @Override
            public void onTick(long millisUntilFinished) {
                progress++;
                progressbar.setProgress(progress);
            }
        }.start();
    }

    private void closeScreen() {
        Intent intent = new Intent();
        intent.setClass(this, color.class);
        startActivity(intent);
        finish();
    }

}
