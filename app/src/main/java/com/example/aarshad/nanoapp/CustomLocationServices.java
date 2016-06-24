package com.example.aarshad.nanoapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class CustomLocationServices extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener, GoogleMap.InfoWindowAdapter, GoogleMap.OnMarkerClickListener {

    Firebase fRef ;
    Firebase locationRef;

    File destination;
    String imagePath;
    FirebaseStorage storage ;
    StorageReference storageRef ;

    private static final int REQUEST_IMAGE = 100;
    public static final String MyPREFERENCES = "PREF" ;
    public static final String userID = "UserID";

    SharedPreferences sharedPreferences ;
    String signedInID ;

    LatLng roppongiHills = new LatLng(35.6604638,139.727060);

    GoogleMap map ;

    Calendar cal ;

    Bitmap bitmapObj = null;
    Bitmap resizedBitmapImg ;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow_menu,menu);
        return true;
    }

    Date currentLocalTime ;
    DateFormat date ;
    String localTime ;

    View view ;
    TextView tvLat ;
    TextView tvLng ;
    TextView tvMarkerID ;
    String markerID ;

    Marker currentMarker ;
    Marker previousMarker ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_location);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.cMap);
        mapFragment.getMapAsync(this);

        fRef = new Firebase("https://scorching-heat-2364.firebaseio.com/");
        locationRef = fRef.child("locations");

        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        signedInID = sharedPreferences.getString(userID,"");

        view = getLayoutInflater().inflate(R.layout.info_window_layout,null);
        tvLat = (TextView) view.findViewById(R.id.tv_lat);
        tvLng = (TextView) view.findViewById(R.id.tv_lng);
        tvMarkerID = (TextView) view.findViewById(R.id.tvMarkerID);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://nanoapp-9233b.appspot.com");

        Long tsLong = System.currentTimeMillis();
        String ts = tsLong.toString();

        destination = new File(Environment.getExternalStorageDirectory(), ts + ".jpg");

    }



    @Override
    public void onMapReady(final GoogleMap googleMap) {
        map = googleMap ;

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(roppongiHills);

        Marker marker = map.addMarker(markerOptions);
        marker.setDraggable(true);
        marker.showInfoWindow();

        previousMarker = marker ;


        final CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(roppongiHills)      // Sets the center of the map to Mountain View
                .zoom(13)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), Math.max(1000, 1),null);

        map.setOnMarkerDragListener(this);
        map.setOnMapLongClickListener(this);
        map.setInfoWindowAdapter(this);
        map.setOnMarkerClickListener(this);

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

        LatLng ltlng = marker.getPosition();
        marker.hideInfoWindow();
        marker.showInfoWindow();
        markerID = marker.getSnippet();
        notifyFirebase(ltlng,markerID);

        Toast.makeText(this,"Started Ended " + marker.getPosition() ,Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        Long tsLong = System.currentTimeMillis();
        String ts = tsLong.toString();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng).snippet(ts);

        Marker marker = map.addMarker(markerOptions);
        marker.setDraggable(true);
        marker.showInfoWindow();



        //markerID = marker.getId();
        currentMarker = marker;

        Polyline line = map.addPolyline(new PolylineOptions()
                .add(new LatLng(previousMarker.getPosition().latitude, previousMarker.getPosition().longitude),
                        new LatLng(currentMarker.getPosition().latitude, currentMarker.getPosition().longitude))
                .width(5)
                .color(Color.RED).geodesic(true));

        previousMarker = currentMarker ;
       notifyFirebase(latLng, ts);

    }

    private void notifyFirebase(LatLng latLng , String mID) {
        cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+9:00"));
        currentLocalTime = cal.getTime();
        date = new SimpleDateFormat("HH:mm:ss");
        date.setTimeZone(TimeZone.getTimeZone("GMT+9:00"));
        localTime = date.format(currentLocalTime);
        locationRef.child(signedInID).child(String.valueOf(mID)).child("Lat").setValue(latLng.latitude);
        locationRef.child(signedInID).child(String.valueOf(mID)).child("Lng").setValue(latLng.longitude);
        locationRef.child(signedInID).child(String.valueOf(mID)).child("Time").setValue(localTime.toString());
        locationRef.child(signedInID).child(String.valueOf(mID)).child("Timestamp").setValue(Long.parseLong(mID));

    }
    private void addPicUrl (String uri , Marker marker) {
        locationRef.child(signedInID).child(String.valueOf(marker.getSnippet())).child("Img:").setValue(uri);
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
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_camera:
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(destination));
                startActivityForResult(intent, REQUEST_IMAGE);
                //currentMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.camera_icon));

                break;
        }
        return  true ;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK ){
            try {


                imagePath = destination.getAbsolutePath();
                Uri file = Uri.fromFile(new File(imagePath));
                UploadTask uploadTask ;

                StorageReference riversRef = storageRef.child("images/"+file.getLastPathSegment());

                // Scaling down the taken picture in order to show on Google Map.
                bitmapObj = BitmapFactory.decodeFile(imagePath);
                resizedBitmapImg = Bitmap.createScaledBitmap(bitmapObj, 100, 100, false);
                currentMarker.setIcon(BitmapDescriptorFactory.fromBitmap(resizedBitmapImg));

                addPicUrl("images/"+file.getLastPathSegment(),currentMarker);

                // Uploading original file to firebase storage
                uploadTask = riversRef.putFile(file);

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
                        Toast.makeText(getApplicationContext(), "Uri: " + downloadUrl,Toast.LENGTH_LONG).show();
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        else{
           // tvPath.setText("Request cancelled");
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        currentMarker = marker ;
        return false;
    }
}
