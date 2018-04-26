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
    private static final String TAG = "onCreate";
    private GoogleMap mMap;
    private ArrayList<Marker> activePolygonMarker;
    private final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 42;
    private boolean polygonSwitch = false;
    private SharedPreferences.Editor myEditor = null;
    private SharedPreferences sharedPref;

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
                mMap.clear();
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
        mMap = googleMap;
        final Activity thisActivity = this;
        checkPermission(thisActivity);
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        myEditor = sharedPref.edit();

        //listen to click events on infoWindow
        mMap.setOnInfoWindowClickListener(this);

        //listen to click events on marker
        mMap.setOnMarkerClickListener(this);

        //TODO: we are not using this one right?
        //Answer: yepp, but maybe later - TODO: we'll delete it if we don't use it when we finished the app
        //create new marker where the map is clicked
//        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//            @Override
//            public void onMapClick(LatLng point) {
//
//            }
//        });

        //on long click
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                //inputText into string
                TextView markerInputText = findViewById(R.id.inputText);
                String newString = getInputText(markerInputText);
                Marker newMarker = mMap.addMarker(new MarkerOptions()
                        .position(point)
                        .title(newString));

                //String foo = '';
                if (polygonSwitch) {
                    activePolygonMarker.add(newMarker);
                    //arrayToString();
                    //foo = foo + savePolygon(point, newString);
                } else {
                    savePolygon(arrayToString());
                    saveMarker(point, newString);
                }
            }
        });
        loadMarkers();
        //loadPolygon();
    }

    private String arrayToString() {
        String polyString = "";
        if (activePolygonMarker.size() != 0) {
            for (int i = 0; i < activePolygonMarker.size(); i++) {
                Double aLat = activePolygonMarker.get(i).getPosition().latitude;
                Double aLng = activePolygonMarker.get(i).getPosition().longitude;
                String aTitle = activePolygonMarker.get(i).getTitle();
                if (polyString == "") {
                    polyString = aTitle + ", " + aLat.toString() + ", " + aLng.toString();
                } else {
                    polyString = polyString + ", " + aTitle + ", " + aLat.toString() + ", " + aLng.toString();
                }
            }
        }
        return polyString;

    }

    private void savePolygon(String polygonString) {
        Integer polygonStringId = polygonString.hashCode();
        myEditor.putString(polygonStringId.toString(), polygonString);
        myEditor.apply();
    }


    private void saveMarker(LatLng p, String title) {
        Double aLat = p.latitude;
        Double aLng = p.longitude;
        String titleLatLng = title + ", " + aLat.toString() + ", " + aLng.toString();
        Integer markerId = titleLatLng.hashCode();

        myEditor.putString(markerId.toString(), titleLatLng);
        //better use apply because we won't need a return value, for which you would use commit()
        myEditor.apply();

    }


/**
    private void loadPolygon() {
        //call the saved polygon data
        Map<String, ?> polyMarker = sharedPref.getAll();
    }
 */

    private void loadMarkers() {
        Map<String, ?> allMarker = sharedPref.getAll();
        for (Map.Entry<String, ?> entry : allMarker.entrySet()) {
            String[] foobar = entry.getValue().toString().split(", ");
            if (foobar.length == 2 || foobar.length == 3) {
                String title = foobar[0];
                Double lat = Double.parseDouble(foobar[1]);
                Double lng = Double.parseDouble(foobar[2]);

                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lng))
                        .title(title)
                );
            }

            else {
                Log.d(TAG, "loadMarkers: polygon");
                String title = foobar[0];
                Double pLat = Double.parseDouble(foobar[1]);
                Double pLng = Double.parseDouble(foobar[2]);

                //recreate marker
                Marker polygonMarker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(pLat, pLng))
                        .title(title)
                );

                activePolygonMarker.add(polygonMarker);

                }


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
                ActivityCompat.requestPermissions(thisActivity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                );
//            }
        } else {
            mMap.setMyLocationEnabled(true);
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
