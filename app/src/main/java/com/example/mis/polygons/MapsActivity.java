package com.example.mis.polygons;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMarkerClickListener
        {

    //variables
    private GoogleMap mMap;
    private final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 42;

    /**Customizing the info window and its contents as shown in this sample code
     * https://github.com/googlemaps/android-samples/blob/master/ApiDemos/java/app/src/main/java/com/example/mapdemo/MarkerDemoActivity.java
     */

    // added for future use - if another activity needs to know this value
    public int getMY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION() {
        return MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        final Activity thisActivity = this;
        mMap = googleMap;

        checkPermission(thisActivity);

        mMap.setMyLocationEnabled(true);

        //inputText into string
        //TODO: do not know why we had this (textview and newstring) here if we ignore them
        //TODO: by creating new ones in onMapLongClick()
        //final TextView marketInputText = findViewById(R.id.inputText);
        //String newString = marketInputText.toString();

        //listen to click events on infoWindow
        mMap.setOnInfoWindowClickListener(this);

        //listen to click events on marker
        mMap.setOnMarkerClickListener(this);

        //create new marker where the map is clicked
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override public void onMapClick(LatLng point) {

            }
        });

        //on long click
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                //inputText into string
                TextView marketInputText = findViewById(R.id.inputText);
                String newString = marketInputText.getText().toString();

                mMap.addMarker(new MarkerOptions()
                        .position(point)
                        .title(newString));
            }
        });
    }

    //on click on the marker
    /** I think this part should be within the onMapReady just like the setOnMapLongClickListener */
    @Override
    public boolean onMarkerClick(Marker marker) {
        //show given markers snippet in a Toast as text
        //TODO: why false?
        marker.showInfoWindow();
        return false;
    }

    public void checkPermission(Activity thisActivity) {
        // followed this guide https://developer.android.com/training/permissions/requesting.html
        //permission added to manifest
        if (ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(thisActivity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                );
            }
        }

    }

    // followed this guide https://developer.android.com/training/permissions/requesting.html
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "info window is clicked and closed",
                Toast.LENGTH_SHORT).show();
        //TODO: do some stuff here
        marker.hideInfoWindow();
    }


}
