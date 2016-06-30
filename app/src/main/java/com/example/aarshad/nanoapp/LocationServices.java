package com.example.aarshad.nanoapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.text.DateFormat;
import java.util.TimeZone;
import java.util.Date;


public class LocationServices extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.InfoWindowAdapter, GoogleMap.OnMarkerClickListener {

    Firebase fRef ;
    Firebase locationRef  ;

    LocationManager locationManager;
    LocationListener locationListener;
    String signedInID ;

    GoogleMap gMap;
    Marker currentMarker ;
    Marker previousMarker ;

    Button btnSendLocation  ;
    Button btnStopLocation ;
    View view ;
    TextView tvLat ;
    TextView tvLng ;
    TextView tvMarkerID ;
    String markerID ;

    Long tsLong;
    String timeString;

    Calendar cal ;
    Date currentLocalTime ;
    DateFormat date ;
    String localTime ;

    File destination;
    String imagePath;
    FirebaseStorage storage ;
    StorageReference storageRef ;
    Bitmap bitmapObj = null;
    Bitmap resizedBitmapImg ;

    private static final int REQUEST_IMAGE = 100;
    public static final String MyPREFERENCES = "PREF" ;
    public static final String userID = "UserID";
    public static final String TAG = "NanoApp";

    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Firebase.setAndroidContext(this);
        fRef = new Firebase("https://scorching-heat-2364.firebaseio.com/");
        locationRef = fRef.child("locations");

        btnSendLocation = (Button) findViewById(R.id.btnSendLocation);
        btnStopLocation = (Button) findViewById(R.id.btnStopLocation);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_services);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sharedpreferences = getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);
        signedInID = sharedpreferences.getString(userID,"");

        view = getLayoutInflater().inflate(R.layout.info_window_layout,null);
        tvLat = (TextView) view.findViewById(R.id.tv_lat);
        tvLng = (TextView) view.findViewById(R.id.tv_lng);
        tvMarkerID = (TextView) view.findViewById(R.id.tvMarkerID);

        tsLong = System.currentTimeMillis();
        timeString = tsLong.toString();

        destination = new File(Environment.getExternalStorageDirectory(), timeString + ".jpg");

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://nanoapp-9233b.appspot.com");

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

                tsLong = System.currentTimeMillis();
                timeString = tsLong.toString();

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(ltlng).snippet(timeString);

                Marker marker = gMap.addMarker(markerOptions);
                marker.setDraggable(true);
                marker.showInfoWindow();

                currentMarker = marker;

                gMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(previousMarker.getPosition().latitude, previousMarker.getPosition().longitude),
                                new LatLng(currentMarker.getPosition().latitude, currentMarker.getPosition().longitude))
                        .width(5)
                        .color(Color.RED).geodesic(true));

                previousMarker = currentMarker ;

                notifyFirebase(ltlng, marker.getSnippet());


            }
            private void notifyFirebase(LatLng latLng , String mID) {

                cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+9:00"));
                currentLocalTime = cal.getTime();
                date = new SimpleDateFormat("HH:mm:ss");
                date.setTimeZone(TimeZone.getTimeZone("GMT+9:00"));
                localTime = date.format(currentLocalTime);

                locationRef.child(signedInID+"_"+mID).child("Lat").setValue(latLng.latitude);
                locationRef.child(signedInID+"_"+mID).child("Lng").setValue(latLng.longitude);
                locationRef.child(signedInID+"_"+mID).child("Time").setValue(localTime.toString());
                locationRef.child(signedInID+"_"+mID).child("Timestamp").setValue(Long.parseLong(mID));
                locationRef.child(signedInID+"_"+mID).child("Uid").setValue(signedInID);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow_menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_camera:
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(destination));
                startActivityForResult(intent, REQUEST_IMAGE);
                break;

        }
        return true ;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK ){

            try {

                imagePath = destination.getAbsolutePath();
                Uri file = Uri.fromFile(new File(imagePath));
                UploadTask uploadTask ;

                StorageReference imagesRef = storageRef.child("images/"+currentMarker.getSnippet()+".jpg");

                // Scaling down the taken picture in order to show on Google Map.
                bitmapObj = BitmapFactory.decodeFile(imagePath);
                resizedBitmapImg = Bitmap.createScaledBitmap(bitmapObj, 100, 100, false);
                // Setting the Bitmap to Marker Icon
                currentMarker.setIcon(BitmapDescriptorFactory.fromBitmap(resizedBitmapImg));

                addPicUrl(currentMarker);

                Log.v(TAG,"Image Uploaded");
                // Uploading original file to firebase storage
                uploadTask = imagesRef.putFile(file);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        // Toast.makeText(getApplicationContext(), "Uri: " + downloadUrl,Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(), "Image Uploaded !",Toast.LENGTH_LONG).show();
                        Log.v(TAG,"Image Uploaded");
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        else{
            Log.v(TAG,"Request cancelled");
        }
    }
    private void addPicUrl ( Marker marker) {
        locationRef.child(signedInID+"_"+marker.getSnippet()).child("Image").setValue("images/"+marker.getSnippet()+".jpg");
    }

    @Override
    public void onMapReady(GoogleMap map) {

        gMap = map ;
        btnSendLocation = (Button) findViewById(R.id.btnSendLocation);
        btnStopLocation = (Button) findViewById(R.id.btnStopLocation);

        gMap.setOnMarkerClickListener(this);

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
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(pLtLng).snippet(timeString);

            Marker marker = gMap.addMarker(markerOptions);
            marker.setDraggable(true);
            marker.showInfoWindow();

            previousMarker = marker ;

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
                    btnStopLocation.setVisibility(View.VISIBLE);
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
        locationManager.requestLocationUpdates("gps", 20000, 1, locationListener);

    }

    public void startSendLocations (View view){


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
    public void stopSendLocations (View view){
        locationManager.removeUpdates(locationListener);
        Toast.makeText(this,"Location Updates Stopped", Toast.LENGTH_SHORT);
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

    @Override
    public boolean onMarkerClick(Marker marker) {

        marker.showInfoWindow();

        return false;
    }
}
