package com.example.mis.polygons;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
        GoogleMap.OnMarkerClickListener {

    //variables
    private GoogleMap mMap;

    /**Customizing the info window and its contents as shown in this sample code
     * https://github.com/googlemaps/android-samples/blob/master/ApiDemos/java/app/src/main/java/com/example/mapdemo/MarkerDemoActivity.java
     */

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
        // followed this guide https://developer.android.com/training/permissions/requesting.html
        if (ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 42;
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(thisActivity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                        );
                //TODO: nothing is checking against MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            }
        }
        mMap.setMyLocationEnabled(true);
        //inputText into string
        final TextView marketInputText = findViewById(R.id.inputText);
        String newString = marketInputText.toString();

        //listen to click events
        mMap.setOnInfoWindowClickListener(this);

        //create new marker where the map is clicked
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override public void onMapClick(LatLng point) {
                //inputText into string
                TextView marketInputText = findViewById(R.id.inputText);
                String newString = marketInputText.toString();


                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(point)
                        .snippet(newString));

            }
        });

        //on long click
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
            }
        });
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "info window is clicked",
                Toast.LENGTH_SHORT).show();
        //TODO: do some stuff here
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        //show given markers snippet in a Toast as text
        Toast.makeText(this, marker.getSnippet(),
                Toast.LENGTH_SHORT).show();
        return false;
    }

}
