package com.example.aarshad.nanoapp;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HistoryLocationShow extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.InfoWindowAdapter {

    LatLng latLng ;
    String userName;
    String time;
    MarkerOptions markerOptions;
    Marker marker;

    View view ;
    TextView tvPostalCode;
    TextView tvAddress;
    TextView tvTime;
    TextView tvName;
    TextView tvLat ;
    TextView tvLng ;
    Geocoder geocoder;
    List<Address> addresses;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_location_show);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.historyMapFragment);
        mapFragment.getMapAsync(this);

        Bundle b = getIntent().getExtras();
        if (b!=null){
            latLng = new LatLng(b.getDouble("lat"),b.getDouble("lng"));
            userName = b.getString("name");
            time = b.getString("time");
        }

        view = getLayoutInflater().inflate(R.layout.info_window_layout,null);
        tvAddress = (TextView) view.findViewById(R.id.tv_Address);
        tvPostalCode = (TextView) view.findViewById(R.id.tv_postalCode);
        tvName = (TextView) view.findViewById(R.id.tv_username);
        tvTime = (TextView)view.findViewById(R.id.tv_time);
        geocoder = new Geocoder(this, Locale.getDefault());

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        markerOptions = new MarkerOptions();
        markerOptions.position(latLng);

        marker = googleMap.addMarker(markerOptions);

        marker.setDraggable(true);
        marker.showInfoWindow();


        final CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to Mountain View
                .zoom(15)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), Math.max(1500, 1),null);

        googleMap.setInfoWindowAdapter(this);

    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {

        LatLng latLng = marker.getPosition();

        try {

            tvName.setText("Username: " + userName);
            tvTime.setText("Time: " + time);
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            tvPostalCode.setText("Postal Code: " + addresses.get(0).getPostalCode());
            tvAddress.setText("Address: " + addresses.get(0).getAddressLine(0));


        } catch (IOException e) {
            e.printStackTrace();
        }


        return view;

    }
}
