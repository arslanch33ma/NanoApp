package com.example.aarshad.nanoapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.text.DateFormat;
import java.util.TimeZone;
import java.util.Date;

public class LocationServices extends AppCompatActivity implements OnMapReadyCallback {

    Firebase baseURL ;
    Firebase fRef ;
    Firebase messagesRef  ;

    LocationManager locationManager;
    LocationListener locationListener;


    Button btnSendLocation  ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Firebase.setAndroidContext(this);
        fRef = new Firebase("https://torrid-inferno-2386.firebaseio.com/List");
        messagesRef = fRef.child("messages");

        btnSendLocation = (Button) findViewById(R.id.btnSendLocation);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_services);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {

            // called when location is updated
            @Override
            public void onLocationChanged(Location location) {
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
                Date currentLocalTime = cal.getTime();
                DateFormat date = new SimpleDateFormat("HH:mm");

                date.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));

                String localTime = date.format(currentLocalTime);

               Toast.makeText(getApplicationContext(),  "OnLocationChange: " + location.getLatitude(), Toast.LENGTH_SHORT).show();
                messagesRef.child("User1").child(localTime.toString()).setValue(location.getLatitude() + " " + location.getLongitude());

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            // called if GPS is OFF
            @Override
            public void onProviderDisabled(String provider) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);

            }
        };

    }

    @Override
    public void onMapReady(GoogleMap map) {

        btnSendLocation = (Button) findViewById(R.id.btnSendLocation);
        LocationManager lm = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);

        Location l = null;

        for (int i=providers.size()-1; i>=0; i--) {
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) break;
        }

        LatLng pLtLng = null ;

        double[] gps = new double[2];
        if (l != null) {
            gps[0] = l.getLatitude();
            gps[1] = l.getLongitude();
            pLtLng = new LatLng(gps[0], gps[1]);
        }


        if (pLtLng!=null) {
            map.addMarker(new MarkerOptions().position(pLtLng).title("Marker"));
            final CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(pLtLng)      // Sets the center of the map to Mountain View
                    .zoom(13)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), Math.max(3500, 1), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    btnSendLocation.setVisibility(View.VISIBLE);

                }

                @Override
                public void onCancel() {

                }
            });
        }
        else {
            btnSendLocation.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    configureButton();
                    return;
                }
        }
    }
    private void configureButton() {
        locationManager.requestLocationUpdates("gps", 15000, 0, locationListener);
    }

    public void startSendLocations (View view){
        Toast.makeText(this,"In Functions",Toast.LENGTH_SHORT).show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, 10);
                return;
            }
        } else
        {
            configureButton();
        }

    }

}
