package com.example.olga.polygonprj;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.kml.KmlLayer;
import com.google.maps.android.kml.KmlPolygon;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.DecimalFormat;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Marker mCurrLocationMarker;
    private LocationRequest mLocationRequest;
    private LocationManager mLocationManager;
    private KmlPolygon kmlPolygon = null;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 99;
    private final static int PERMISSIONS_REQUEST_LOCATION = 999;
    private final static int GPS_REQUEST = 9999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        checkLocationPermission();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        KmlLayer mKmlLayer;

        // Reads KML file
        try {
            mKmlLayer = new KmlLayer(mMap, R.raw.allowed_area, getApplicationContext());

            // Adds mKmlLayer to map and gets polygon from KML layer
            if (mKmlLayer != null) {
                mKmlLayer.addLayerToMap();
                kmlPolygon = PolygonUtil.getPolygon(mKmlLayer.getContainers().iterator().next());
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize Google Play Services
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    public boolean checkLocationPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_LOCATION);
            }
            return false;

        } else if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) { // checks if GPS enabled
            openGpsSettings();
            return false;

        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // If permission approved
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    if (mGoogleApiClient == null) {
                        buildGoogleApiClient();
                    }

                    if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        openGpsSettings();
                    }

                    mMap.setMyLocationEnabled(true);
                }

            } else {

                // If permission was denied
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openGpsSettings() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(intent, GPS_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Checks if GPS enabled or no
        if (resultCode == 0) {
            if (requestCode == GPS_REQUEST) {
                if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Toast.makeText(this, R.string.gps_disabled, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        buildLocationRequest();

        // Requests location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Starts an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, R.string.generel_err_msg, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        LatLng myLocation = new LatLng(latitude, longitude);

        // Adds marker on current location
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(myLocation);
        markerOptions.title(getString(R.string.map_marker_title));
        mCurrLocationMarker = mMap.addMarker(markerOptions);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));  //zoom map to myLocation

        // Tells the user if he is inside polygon or outside.
        // If outside, shows the shortest distance as the message
        showUserDistanceFromPolygon(myLocation);


        // Stops location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    private void buildLocationRequest() {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1 * 1000);        // 1 seconds, in milliseconds
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    // Tells the user if he is inside polygon or outside.
    // If outside, shows the shortest distance as the message
    private void showUserDistanceFromPolygon(LatLng myLocation) {

        if (kmlPolygon == null || myLocation == null) {
            return;
        }

        boolean liesInside = PolygonUtil.liesOnPolygon(kmlPolygon, myLocation);

        if (liesInside) {
            Toast.makeText(this, R.string.inside_polygon, Toast.LENGTH_LONG).show();
        } else {
            // Return the shortest distance from current location to polygon
            double shortestDistance = PolygonUtil.findShortestDistance(kmlPolygon, myLocation);
            DecimalFormat decimalFormat = new DecimalFormat(".00");
            Toast.makeText(this,
                    getString(R.string.outside_polygon)
                            + decimalFormat.format(shortestDistance)
                            + getString(R.string.meter),
                    Toast.LENGTH_LONG).show();
        }
    }
}



