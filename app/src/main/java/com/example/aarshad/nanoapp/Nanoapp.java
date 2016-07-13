package com.example.aarshad.nanoapp;

import com.firebase.client.Firebase;

/**
 * Created by Cheema on 6/12/16.
 */
public class NanoApp extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Firebase.setAndroidContext(this);

    }
}
