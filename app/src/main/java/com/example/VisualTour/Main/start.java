package com.example.VisualTour.Main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.VisualTour.R;

public class start extends Activity
{
    int timeout = 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.iniziobius);

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                Intent i = new Intent(start.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, timeout);
    }
}
