package com.example.aarshad.nanoapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CustomLocationServices extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener, GoogleMap.InfoWindowAdapter, GoogleMap.OnMarkerClickListener, GoogleApiClient.OnConnectionFailedListener {

    Firebase fRef ;
    Firebase locationRef;

    File destination;
    String imagePath;
    FirebaseStorage storage ;
    StorageReference storageRef ;

    GoogleApiClient mGoogleApiClient;

    GoogleMap map ;
    Marker currentMarker ;
    Marker previousMarker ;

    Geocoder geocoder;
    List<Address> addresses;

    SharedPreferences sharedPreferences ;
    String signedInID ;

    LatLng roppongiHills = new LatLng(35.6604638,139.727060);

    Bitmap bitmapObj = null;
    Bitmap resizedBitmapImg ;

    Calendar cal ;
    Date currentLocalTime ;
    DateFormat date ;
    String localTime ;

    View view ;
    TextView tvPostalCode;
    TextView tvAddress;
    TextView tvLat ;
    TextView tvLng ;

    Long tsLong;
    String timeString;

    MyDBHandler dbHandler;

    private static final int REQUEST_IMAGE = 100;
    public static final String MyPREFERENCES = "PREF" ;
    public static final String userID = "UserID";
    public static final String TAG = "nanoapp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_location);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.cMap);
        mapFragment.getMapAsync(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso).build();

        fRef = new Firebase("https://scorching-heat-2364.firebaseio.com/");
        locationRef = fRef.child("locations");

        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        signedInID = sharedPreferences.getString(userID,"");

        view = getLayoutInflater().inflate(R.layout.info_window_layout,null);
        tvLat = (TextView) view.findViewById(R.id.tv_lat);
        tvLng = (TextView) view.findViewById(R.id.tv_lng);
        tvAddress = (TextView) view.findViewById(R.id.tv_Address);
        tvPostalCode = (TextView) view.findViewById(R.id.tv_postalCode);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://scorching-heat-2364.appspot.com");

        tsLong = System.currentTimeMillis();
        timeString = tsLong.toString();

        geocoder = new Geocoder(this, Locale.getDefault());

        destination = new File(Environment.getExternalStorageDirectory(), timeString + ".jpg");

        dbHandler = new MyDBHandler(this, null, null, 1);


    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        map = googleMap ;

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(roppongiHills);

        Marker marker = map.addMarker(markerOptions);
        marker.setDraggable(true);
        marker.showInfoWindow();

        currentMarker = marker ;
        previousMarker = marker ;

        final CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(roppongiHills)      // Sets the center of the map to Mountain View
                .zoom(13)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), Math.max(2500, 1),new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
               Toast.makeText(CustomLocationServices.this,"Put Markers by Long Click",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {

            }
        });

        map.setOnMapLongClickListener(this);
        map.setInfoWindowAdapter(this);
        map.setOnMarkerClickListener(this);

    }


    @Override
    public void onMapLongClick(LatLng latLng) {

        tsLong = System.currentTimeMillis();
        timeString = tsLong.toString();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng).snippet(timeString);

        Marker marker = map.addMarker(markerOptions);
        marker.setDraggable(true);
        marker.showInfoWindow();

        currentMarker = marker;

        map.addPolyline(new PolylineOptions()
                .add(new LatLng(previousMarker.getPosition().latitude, previousMarker.getPosition().longitude),
                        new LatLng(currentMarker.getPosition().latitude, currentMarker.getPosition().longitude))
                .width(5)
                .color(Color.RED).geodesic(true));

        previousMarker = currentMarker ;

       notifyFirebase(latLng, marker.getSnippet());

        insertIntoDb(latLng);

    }

    private void insertIntoDb(LatLng latlng) {

        addresses = null ;

        try {
            addresses = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        LocationInfo locInfo = new LocationInfo(signedInID,String.valueOf(latlng.latitude),String.valueOf(latlng.longitude),addresses.get(0).getAddressLine(0));
        dbHandler.insertLocation(locInfo);
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
    private void addPicUrl ( Marker marker) {
        locationRef.child(signedInID+"_"+marker.getSnippet()).child("Image").setValue("images/"+marker.getSnippet()+".jpg");
    }


    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {

        LatLng latLng = marker.getPosition();

        try {

            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            tvPostalCode.setText("Postal Code: " + addresses.get(0).getPostalCode());
            tvLat.setText("Lat: " +  latLng.latitude);
            tvLng.setText("Lng: " + latLng.longitude);
            tvAddress.setText("Address: " + addresses.get(0).getAddressLine(0));

        } catch (IOException e) {
            marker.hideInfoWindow();
            e.printStackTrace();
        }


        return view;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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

            case R.id.action_history:
                Intent intent_history = new Intent(this,History.class);
                startActivity(intent_history);
                break;

            case R.id.menu_logout:
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                Toast.makeText(getApplicationContext(),"Logged out", Toast.LENGTH_LONG).show();
                                Intent i = new Intent(getApplicationContext(), Authentication.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                                finishAffinity();

                            }
                        }
                );
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

                        addPicUrl(currentMarker);
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        // Toast.makeText(getApplicationContext(), "Uri: " + downloadUrl,Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(), "Image Uploaded !",Toast.LENGTH_LONG).show();
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


    @Override
    public boolean onMarkerClick(Marker marker) {
        currentMarker = marker ;
        return false;
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v(TAG,"Connection Failed");
    }
}
