package com.example.aarshad.nanoapp;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
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


public class LocationServices extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.InfoWindowAdapter, GoogleMap.OnMarkerClickListener, GoogleApiClient.OnConnectionFailedListener {

    Firebase fRef ;
    Firebase locationRef  ;

    GoogleApiClient mGoogleApiClient;

    LocationManager locationManager;
    LocationListener locationListener;
    String signedInID ;
    String signedInName;

    GoogleMap gMap;
    Marker currentMarker ;
    Marker previousMarker ;
    CameraPosition cameraPosition;

    Button btnSendLocation  ;
    Button btnStopLocation ;

    View view ;
    TextView tvPostalCode;
    TextView tvAddress;
    TextView tvTime ;
    TextView tvName ;

    MarkerOptions markerOptions;
    Marker marker;

    Long tsLong;
    String timeString;
    double lat;
    double lng;
    LatLng latlngDb ;
    LatLng newPosition;

    Calendar cal ;
    Date currentLocalTime ;
    DateFormat date ;
    String localTime ;

    Geocoder geocoder;
    List<Address> addresses;

    File destination;
    String imagePath;
    FirebaseStorage storage ;
    StorageReference storageRef ;
    Bitmap bitmapObj = null;
    Bitmap resizedBitmapImg ;
    MyDBHandler dbHandler;
    Cursor c;
    ProgressDialog progDailog;

    private static final int REQUEST_IMAGE = 100;
    public static final String MyPREFERENCES = "PREF" ;
    public static final String userID = "UserID";
    public static final String userName = "UserName";
    public static final String CONTENTS_STATUS = "MapContents";
    public static final String TAG = "NanoApp";

    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor ;

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

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso).build();

        sharedpreferences = getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);
        signedInID = sharedpreferences.getString(userID,"");
        signedInName = sharedpreferences.getString(userName,"");
        editor = sharedpreferences.edit();

        view = getLayoutInflater().inflate(R.layout.info_window_layout,null);
        tvTime = (TextView) view.findViewById(R.id.tv_time);
        tvName = (TextView) view.findViewById(R.id.tv_username);
        tvAddress = (TextView) view.findViewById(R.id.tv_Address);
        tvPostalCode = (TextView) view.findViewById(R.id.tv_postalCode);

        tsLong = System.currentTimeMillis();
        timeString = tsLong.toString();

        geocoder = new Geocoder(this, Locale.getDefault());


        destination = new File(Environment.getExternalStorageDirectory(), timeString + ".jpg");
        dbHandler = new MyDBHandler(this, null, null, 1);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://scorching-heat-2364.appspot.com");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {

            // called when location is updated
            @Override
            public void onLocationChanged(Location location) {

                LatLng ltlng = new LatLng(location.getLatitude(),location.getLongitude());

                tsLong = System.currentTimeMillis();
                timeString = tsLong.toString();

                markerOptions = new MarkerOptions();
                markerOptions.position(ltlng).snippet(timeString);

                marker = gMap.addMarker(markerOptions);
                marker.setDraggable(true);
                marker.showInfoWindow();

                cameraPosition = new CameraPosition.Builder()
                        .target(ltlng).zoom(15).bearing(0).tilt(30).build();

                gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), Math.max(1000, 1),null);

                currentMarker = marker;

                gMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(previousMarker.getPosition().latitude, previousMarker.getPosition().longitude),
                                new LatLng(currentMarker.getPosition().latitude, currentMarker.getPosition().longitude))
                        .width(5)
                        .color(Color.RED).geodesic(true));

                previousMarker = currentMarker ;

                editor.putString(CONTENTS_STATUS, "Contents");
                editor.commit();

                notifyFirebase(ltlng, marker.getSnippet());

                insertIntoDb(ltlng);

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

                Toast.makeText(LocationServices.this,"GPS is turned off ... ",Toast.LENGTH_LONG).show();

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);

            }
        };

    }
    @Override
    protected void onResume() {
        String contentsStatus = sharedpreferences.getString(CONTENTS_STATUS,"");
        if (contentsStatus == "Clear"){
            if (gMap!=null) {
                gMap.clear();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow_menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_logout:
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                Toast.makeText(getApplicationContext(),"Logged out", Toast.LENGTH_LONG).show();
                                Intent i = new Intent(getApplicationContext(), Authentication.class);
                                startActivity(i);
                                finishAffinity();
                            }
                        }
                );
                break;
            case R.id.action_camera:
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(destination));
                startActivityForResult(intent, REQUEST_IMAGE);
                break;
            case R.id.action_history:
                Intent intent_history = new Intent(this,History.class);
                startActivity(intent_history);
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

                        addPicUrl(currentMarker);
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

        progDailog = new ProgressDialog(LocationServices.this);
        progDailog.setMessage("Loading ...");
        progDailog.setIndeterminate(true);
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDailog.setCancelable(true);
        progDailog.show();


        gMap = map ;
        btnSendLocation = (Button) findViewById(R.id.btnSendLocation);
        btnStopLocation = (Button) findViewById(R.id.btnStopLocation);

        gMap.setOnMarkerClickListener(this);
        gMap.setInfoWindowAdapter(this);

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

            cameraPosition = new CameraPosition.Builder()
                    .target(pLtLng)      // Sets the center of the map to Mountain View
                    .zoom(13)                   // Sets the zoom
                    .bearing(0)                // Sets the orientation of the camera to east
                    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();
            gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), Math.max(1000, 1), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    btnSendLocation.setVisibility(View.VISIBLE);
                    btnStopLocation.setVisibility(View.VISIBLE);
                    new LoadData().execute();
                }

                @Override
                public void onCancel() {

                }
            });
        }
        else {
            btnSendLocation.setVisibility(View.VISIBLE);
            btnStopLocation.setVisibility(View.VISIBLE);
            progDailog.hide();
        }

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

                        marker = gMap.addMarker(markerOptions);
                        marker.setDraggable(true);
                        marker.showInfoWindow();
                        currentMarker = marker;

                        gMap.addPolyline(new PolylineOptions()
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
            newPosition = new LatLng(previousMarker.getPosition().latitude, previousMarker.getPosition().longitude);
            cameraPosition = new CameraPosition.Builder()
                    .target(newPosition).zoom(15) .bearing(0).tilt(30).build();

            gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), Math.max(1000, 1),null);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    sendUpdates();

                }
        }
    }
    private void sendUpdates() {
        Toast.makeText(LocationServices.this,"Looking for GPS Signals ... ",Toast.LENGTH_LONG).show();
        locationManager.requestLocationUpdates("gps", 20000, 1, locationListener);

    }

    public void startSendLocations (View view){


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Requesting Permissions");
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, 10);

            }
            else
            {
                sendUpdates();
            }

        } else
        {
            Log.v(TAG," Version is less than " + Build.VERSION_CODES.M);
            sendUpdates();
        }

    }
    public void stopSendLocations (View view){

        Toast.makeText(this,"Location Updates Stopped", Toast.LENGTH_SHORT).show();
        locationManager.removeUpdates(locationListener);
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
            tvName.setText("User Name: " + signedInName);
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
    public boolean onMarkerClick(Marker marker) {

        marker.showInfoWindow();

        return false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v(TAG,"Connection Failed");
    }
}
