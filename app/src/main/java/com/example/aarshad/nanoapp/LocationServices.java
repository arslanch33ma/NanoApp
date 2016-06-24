package com.example.aarshad.nanoapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.text.DateFormat;
import java.util.TimeZone;
import java.util.Date;

public class LocationServices extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.InfoWindowAdapter {

    Firebase fRef ;
    Firebase locationRef  ;

    LocationManager locationManager;
    LocationListener locationListener;
    String signedInID ;

    GoogleMap gMap;
    Button btnSendLocation  ;
    View view ;
    TextView tvLat ;
    TextView tvLng ;
    TextView tvMarkerID ;
    String markerID ;

    public static final String MyPREFERENCES = "PREF" ;
    public static final String userID = "UserID";

    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Firebase.setAndroidContext(this);
        fRef = new Firebase("https://scorching-heat-2364.firebaseio.com/");
        locationRef = fRef.child("locations");

        btnSendLocation = (Button) findViewById(R.id.btnSendLocation);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_services);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sharedpreferences = getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);

        view = getLayoutInflater().inflate(R.layout.info_window_layout,null);
        tvLat = (TextView) view.findViewById(R.id.tv_lat);
        tvLng = (TextView) view.findViewById(R.id.tv_lng);
        tvMarkerID = (TextView) view.findViewById(R.id.tvMarkerID);


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {

            // called when location is updated
            @Override
            public void onLocationChanged(Location location) {
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+9:00"));
                Date currentLocalTime = cal.getTime();
                DateFormat date = new SimpleDateFormat("HH:mm");

                date.setTimeZone(TimeZone.getTimeZone("GMT+9:00"));

                String localTime = date.format(currentLocalTime);

                LatLng ltlng = new LatLng(location.getLatitude(),location.getLongitude());

                Long tsLong = System.currentTimeMillis();
                String ts = tsLong.toString();
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(ltlng).snippet(ts);

                Marker marker = gMap.addMarker(markerOptions);
                marker.setDraggable(true);
                marker.showInfoWindow();

                markerID = marker.getSnippet() ;

                locationRef.child(signedInID).child(String.valueOf(markerID)).child("Lat").setValue(ltlng.latitude);
                locationRef.child(signedInID).child(String.valueOf(markerID)).child("Lng").setValue(ltlng.longitude);
                locationRef.child(signedInID).child(String.valueOf(markerID)).child("Time").setValue(localTime.toString());
                locationRef.child(signedInID).child(String.valueOf(markerID)).child("Timestamp").setValue(Long.parseLong(markerID));


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

        gMap = map ;
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
            gMap.addMarker(new MarkerOptions().position(pLtLng).title("Marker"));
            final CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(pLtLng)      // Sets the center of the map to Mountain View
                    .zoom(13)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();
            gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), Math.max(3500, 1), new GoogleMap.CancelableCallback() {
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
        locationManager.requestLocationUpdates("gps", 3000, 1, locationListener);
    }

    public void startSendLocations (View view){

        signedInID = sharedpreferences.getString(userID,"");
        Toast.makeText(this,"Stored ID: " + signedInID ,Toast.LENGTH_SHORT).show();
        if (sharedpreferences.contains(userID)){

        }

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

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        LatLng latLng = marker.getPosition();

        tvLat.setText("Lat: " +  latLng.latitude);
        tvLng.setText("Lng: " + latLng.longitude);
        tvMarkerID.setText("ID:" + marker.getId());

        return view;
    }
}
