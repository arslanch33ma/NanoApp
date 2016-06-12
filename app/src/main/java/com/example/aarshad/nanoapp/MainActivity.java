package com.example.aarshad.nanoapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void locationServices(View view)
    {
        Intent locationServicesIntent = new Intent(this,LocationServices.class);
        startActivity(locationServicesIntent);

    }

}
