package com.amigos.activity;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.amigos.R;
import com.amigos.helpers.GDNSharedPrefrences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by Nirav on 22/12/2015.
 */
public class NewJobRequestActivity extends GDNBaseActivity implements View.OnClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    Double pickupLat;
    Double pickupLon;
    Double dropLat;
    Double dropLon;
    String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_job_request);
        renderNavigationDrawer();
        renderChildActivityToolbar();

        Bundle extras = getIntent().getExtras();
        String jobDetails = extras.getString("details");
        String[] parts = jobDetails.split(":");

        pickupLat = Double.valueOf(parts[1]);
        pickupLon = Double.valueOf(parts[2]);
        dropLat = Double.valueOf(parts[3]);
        dropLon = Double.valueOf(parts[4]);
        address = parts[5];

        buildGoogleApiClient();
        findViewById(R.id.fab).setOnClickListener(this);
        findViewById(R.id.reject_request).setOnClickListener(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.new_request_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        GDNSharedPrefrences.setMap(googleMap);
        GDNSharedPrefrences.getMap().getUiSettings().setMyLocationButtonEnabled(true);
        GDNSharedPrefrences.getMap().setMyLocationEnabled(true);
        GDNSharedPrefrences.getMap().getUiSettings().setScrollGesturesEnabled(false);

        LatLng drop = new LatLng(dropLat,dropLon);
        LatLng pick = new LatLng(pickupLat,pickupLon);
        GDNSharedPrefrences.getMap().addMarker(new MarkerOptions().position(drop).title("Drop"));
        GDNSharedPrefrences.getMap().addMarker(new MarkerOptions().position(pick).title("Pickup"));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(drop);
        builder.include(pick);
        if (GDNSharedPrefrences.getLastLocation() != null) {
            LatLng curPos = new LatLng(GDNSharedPrefrences.getLastLocation().getLatitude(), GDNSharedPrefrences.getLastLocation().getLongitude());
            builder.include(curPos);
            GDNSharedPrefrences.getMap().addMarker(new MarkerOptions().position(curPos).title("You"));
        }

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(builder.build(), 5);
        GDNSharedPrefrences.getMap().animateCamera(cu);
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            GDNSharedPrefrences.setLastLocation(mLastLocation);
            Toast.makeText(NewJobRequestActivity.this, "Location Updated", Toast.LENGTH_LONG).show();
            setupMap();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void setupMap(){
        if (GDNSharedPrefrences.getMap() != null) {
            GDNSharedPrefrences.getMap().getUiSettings().setMyLocationButtonEnabled(true);
            GDNSharedPrefrences.getMap().setMyLocationEnabled(true);
            GDNSharedPrefrences.getMap().getUiSettings().setScrollGesturesEnabled(false);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 15);
            GDNSharedPrefrences.getMap().animateCamera(cameraUpdate);

            GDNSharedPrefrences.getMap().addPolyline(new PolylineOptions()
                                                        .add(new LatLng(pickupLat,pickupLon),
                                                                new LatLng(dropLat,dropLon)).geodesic(true));
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
