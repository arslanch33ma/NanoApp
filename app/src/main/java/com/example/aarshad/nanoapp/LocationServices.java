package com.example.aarshad.nanoapp;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class LocationServices extends AppCompatActivity implements OnMapReadyCallback {

    Button btnSendLocation  ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        btnSendLocation = (Button) findViewById(R.id.btnSendLocation);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_services);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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


    }

}
