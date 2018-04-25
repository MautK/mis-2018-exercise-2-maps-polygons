package com.example.mis.polygons;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.common.server.converter.StringToIntConverter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private ArrayList<Marker> activePolygonMarker;
    private static final String TAG = "MapsActivity";
    private final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 42;
    private boolean polygonSwitch = false;
    private SharedPreferences.Editor myEditor = null;
    private SharedPreferences sharedPref;

    // added for future use - if another activity needs to know this value
    public int getMY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION() {
        return MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        final Button createPolygon = findViewById(R.id.buttonPolygon);
        final Button deletePolygon = findViewById(R.id.buttonDelete);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        deletePolygon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myEditor.clear();
                myEditor.commit();
            }
        });

        createPolygon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (polygonSwitch == false) {
                    createPolygon.setText("End Polygon");
                    activePolygonMarker = new ArrayList<>();
                    polygonSwitch = !polygonSwitch;
                } else {
//                    double area = calcArea();
//                    Log.d(TAG, "onClick: " + foo);
                    createPolygon.setText("Start Polygon");
                    calcCentroid();
                    polygonSwitch = !polygonSwitch;
                }
            }
        });
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        final Activity thisActivity = this;
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        myEditor = sharedPref.edit();

        mMap = googleMap;

        checkPermission(thisActivity);

        mMap.setMyLocationEnabled(true);


        //listen to click events on infoWindow
        mMap.setOnInfoWindowClickListener(this);

        //listen to click events on marker
        mMap.setOnMarkerClickListener(this);

        //TODO: we are not using this one right?
        //Answer: yepp, but maybe later - TODO: we'll delete it if we don't use it when we finished the app
        //create new marker where the map is clicked
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {

            }
        });

        //on long click
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                //inputText into string
                TextView markerInputText = findViewById(R.id.inputText);
                String newString = getInputText(markerInputText);

                //TODO: Should we create this into an array so we can iterate it later for  the polygon action?
                //Answer: created arrayList of type Marker -> activePolygonMarker

                saveMarker(point, newString);

                Marker newMarker = mMap.addMarker(new MarkerOptions()
                        .position(point)
                        .title(newString));

                if (polygonSwitch) {
                    activePolygonMarker.add(newMarker);
                }
            }
        });
        loadMarkers();
    }

    private void saveMarker(LatLng p, String title) {
        Double aLat = p.latitude;
        Double aLng = p.longitude;
        String titleLatLng = title + ", " + aLat.toString() + ", " + aLng.toString();
        Integer markerId = titleLatLng.hashCode();

        myEditor.putString(markerId.toString(), titleLatLng);
        myEditor.commit();
    }

    private void loadMarkers() {
        Map<String, ?> allMarker = sharedPref.getAll();

        for (Map.Entry<String, ?> entry : allMarker.entrySet()) {
            String[] foobar = entry.getValue().toString().split(", ");
            String title = foobar[0];
            Double lat = Double.parseDouble(foobar[1]);
            Double lng = Double.parseDouble(foobar[2]);

            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lng))
                    .title(title)
            );
            Log.d(TAG, "title: " + foobar[0]);
            Log.d(TAG, "lat: " + foobar[1]);
            Log.d(TAG, "lng: " + foobar[2]);
        }
    }

    private String getInputText(TextView v) {
        return v.getText().toString();
    }


    public double calcArea() {
        Polygon newPolygon;
        PolygonOptions newPolygonOptions = new PolygonOptions();

        double area;

        // used method computeArea() from SphericalUtil to get the area enclosed by markers in m^2
        // http://googlemaps.github.io/android-maps-utils/javadoc/com/google/maps/android/SphericalUtil.html
        if (polygonSwitch && activePolygonMarker.size() >= 3) {
            for (int i = 0; i < activePolygonMarker.size(); i++) {
//                if (i+1 < activePolygonMarker.size()) {
                newPolygonOptions.add(activePolygonMarker.get(i).getPosition());
            }
            newPolygon = mMap.addPolygon(newPolygonOptions);
            area = SphericalUtil.computeArea(newPolygon.getPoints());

            newPolygon.setFillColor(0x8881C784);

        } else {
            area = 0;
        }
        return area;
    }

    public String addUnitToArea(Double area) {
        String areaWithUnit;
        if (area >= 100000) {
            area = area/1000000;
            DecimalFormat addSquareKilometers = new DecimalFormat("0.000");
            addSquareKilometers.setRoundingMode(RoundingMode.CEILING);
            areaWithUnit = addSquareKilometers.format(area) +
                    Html.fromHtml("km\u00B2");
        } else {
            DecimalFormat addSquareMeters = new DecimalFormat("0.00");
            addSquareMeters.setRoundingMode(RoundingMode.CEILING);
            areaWithUnit = addSquareMeters.format(area) +
                    Html.fromHtml("m\u00B2");
        }
        return areaWithUnit;
    }
    public void calcCentroid() {
        LatLng centroid;
        double centroidLat = 0;
        double centroidLng = 0;
        if (polygonSwitch && activePolygonMarker.size() >= 3) {
            for (int i = 0; i < activePolygonMarker.size(); i++) {
                centroidLat += activePolygonMarker.get(i).getPosition().latitude;
                centroidLng += activePolygonMarker.get(i).getPosition().longitude;
            }

            //TODO: test this approach
            // https://sciencing.com/convert-xy-coordinates-longitude-latitude-8449009.html
            centroidLat = centroidLat / activePolygonMarker.size();
            centroidLng = centroidLng / activePolygonMarker.size();
            centroid = new LatLng(centroidLat, centroidLng);
            String centroidTitle = addUnitToArea(calcArea());
            mMap.addMarker(new MarkerOptions()
                    .position(centroid)
                    .title(centroidTitle));
        }
    }

    public static void saveObjectToSharedPreferences() {
        //here the code that sets up the sharedPreferences File

        //adding a string to the sharedPreferences
        //apply changes to sharedPreferences
    }

    public static void getSavedObjectFromPreferences() {
        //retrieve the data from the sharedPreferences File
    }


    //on click on the marker
    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return true;
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
        marker.hideInfoWindow();
    }
}
