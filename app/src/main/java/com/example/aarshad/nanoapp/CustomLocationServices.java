package com.example.aarshad.nanoapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
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
    CameraPosition cameraPosition;

    Geocoder geocoder;
    List<Address> addresses;

    SharedPreferences sharedPreferences ;
    SharedPreferences.Editor editor ;
    String signedInID ;
    String signedInName;

    LatLng roppongiHills = new LatLng(35.6604638,139.727060);

    Bitmap bitmapObj = null;
    Bitmap resizedBitmapImg ;

    Calendar cal ;
    Date currentLocalTime ;
    DateFormat date ;
    String localTime ;

    MarkerOptions markerOptions;
    Marker marker;

    View view ;
    TextView tvPostalCode;
    TextView tvAddress;
    TextView tvTime ;
    TextView tvName ;

    Long tsLong;
    String timeString;
    double lat;
    double lng;
    LatLng latlngDb ;

    MyDBHandler dbHandler;
    Cursor c;
    ProgressDialog progDailog;

    private static final int REQUEST_IMAGE = 100;
    public static final String MyPREFERENCES = "PREF" ;
    public static final String userID = "UserID";
    public static final String userName = "UserName";
    public static final String CONTENTS_STATUS = "MapContents";
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
        signedInName = sharedPreferences.getString(userName,"");
        editor = sharedPreferences.edit();

        view = getLayoutInflater().inflate(R.layout.info_window_layout,null);
        tvTime = (TextView) view.findViewById(R.id.tv_time);
        tvName = (TextView) view.findViewById(R.id.tv_username);
        tvAddress = (TextView) view.findViewById(R.id.tv_Address);
        tvPostalCode = (TextView) view.findViewById(R.id.tv_postalCode);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://scorching-heat-2364.appspot.com");

        tsLong = System.currentTimeMillis();
        timeString = tsLong.toString();

        geocoder = new Geocoder(this, Locale.getDefault());

        destination = new File(Environment.getExternalStorageDirectory(), timeString + ".jpg");

        dbHandler = new MyDBHandler(this, null, null, 1);

        progDailog = new ProgressDialog(CustomLocationServices.this);
        progDailog.setMessage("Loading ...");
        progDailog.setIndeterminate(true);
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDailog.setCancelable(true);
        progDailog.show();

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        map = googleMap ;

        tsLong = System.currentTimeMillis();
        timeString = tsLong.toString();

        markerOptions = new MarkerOptions();
        markerOptions.position(roppongiHills).snippet(timeString);

        marker = map.addMarker(markerOptions);
        marker.setDraggable(true);
        marker.showInfoWindow();

        currentMarker = marker ;
        previousMarker = marker ;

         cameraPosition = new CameraPosition.Builder()
                .target(roppongiHills)      // Sets the center of the map to Mountain View
                .zoom(15)                   // Sets the zoom
                .bearing(0)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), Math.max(1000, 1),new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {

                new LoadData().execute();
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

    class LoadData extends AsyncTask<Integer, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        @Override
        protected String doInBackground(Integer... params) {
            c = dbHandler.getData(signedInID);
            return "Task Completed.";
        }
        @Override
        protected void onPostExecute(String result) {
            if (c.getCount()>0){
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    if (c.getString(c.getColumnIndex("uid")) != null) {
                        lat = Double.parseDouble(c.getString(c.getColumnIndex("lat")));
                        lng = Double.parseDouble(c.getString(c.getColumnIndex("lng")));
                        latlngDb = new LatLng(lat,lng);
                        markerOptions = new MarkerOptions();
                        markerOptions.position(latlngDb);

                        marker = map.addMarker(markerOptions);
                        marker.setDraggable(true);
                        marker.showInfoWindow();
                        currentMarker = marker;

                        map.addPolyline(new PolylineOptions()
                                .add(new LatLng(previousMarker.getPosition().latitude, previousMarker.getPosition().longitude),
                                        new LatLng(currentMarker.getPosition().latitude, currentMarker.getPosition().longitude))
                                .width(5)
                                .color(Color.RED).geodesic(true));

                        previousMarker = currentMarker ;
                    }
                    c.moveToNext();
                }

            }
            progDailog.hide();
            LatLng newPosition = new LatLng(previousMarker.getPosition().latitude, previousMarker.getPosition().longitude);
            cameraPosition = new CameraPosition.Builder()
                    .target(newPosition)      // Sets the center of the map to Mountain View
                    .zoom(15)                   // Sets the zoom
                    .bearing(0)                // Sets the orientation of the camera to east
                    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), Math.max(1000, 1),null);
        }

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

        editor.putString(CONTENTS_STATUS, "Contents");
        editor.commit();

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

        LocationInfo locInfo = new LocationInfo(signedInID,signedInName,String.valueOf(latlng.latitude),String.valueOf(latlng.longitude),addresses.get(0).getAddressLine(0),localTime);
        dbHandler.insertLocation(locInfo);

    }


    private void notifyFirebase(LatLng latLng , String mID) {

        cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+9:00"));
        currentLocalTime = cal.getTime();
        date = new SimpleDateFormat("MMM, dd HH:mm:ss");
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

            tvName.setText("Username: " + signedInName);
            tvTime.setText("Locality/Ward: " +  addresses.get(0).getLocality());
            tvPostalCode.setText("Postal Code: " + addresses.get(0).getPostalCode());
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
    protected void onResume() {
        String contentsStatus = sharedPreferences.getString(CONTENTS_STATUS,"");
        if (contentsStatus == "Clear"){
            if (map!=null) {
                map.clear();
            }
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        progDailog.dismiss();

        super.onPause();
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
