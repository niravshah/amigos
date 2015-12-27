package com.amigos.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.amigos.R;
import com.amigos.helpers.GDNApiHelper;
import com.amigos.helpers.GDNConstants;
import com.amigos.helpers.GDNSharedPrefrences;
import com.amigos.helpers.GDNVolleySingleton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

/**
 * Created by Nirav on 18/12/2015.
 */
public class MainActivity extends GDNBaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, OnMapReadyCallback {

    private GoogleApiClient mGoogleApiClient;
    private BroadcastReceiver gcmBroadcastReceiver;
    private Location mLastLocation;
    Button onlineBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Toast.makeText(MainActivity.this, "Logged In: " + GDNSharedPrefrences.getAcctName(), Toast.LENGTH_LONG).show();
        setContentView(R.layout.activity_main);
        renderToolbarActionbar();
        renderNavigationDrawer();
        initializeBroadcastReciever();
        buildGoogleApiClient();

        onlineBtn = (Button) findViewById(R.id.go_online);
        onlineBtn.setOnClickListener(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.ninja_location_map);
        mapFragment.getMapAsync(this);

        GDNSharedPrefrences.setCurrentState(GDNConstants.ONLINE);
        onlineBtn.setText("GO OFFLINE");



    }

    private void initializeBroadcastReciever() {
        gcmBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(GDNSharedPrefrences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Toast.makeText(MainActivity.this, "GCM Token Sent", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "GCM Token Error", Toast.LENGTH_LONG).show();
                }
            }
        };
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

        //Toast.makeText(MainActivity.this, "Google API Connected", Toast.LENGTH_LONG).show();
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            GDNSharedPrefrences.setLastLocation(mLastLocation);
            //Toast.makeText(MainActivity.this, "Location Updated", Toast.LENGTH_LONG).show();
            if (GDNSharedPrefrences.getMap() != null) {
                GDNSharedPrefrences.getMap().getUiSettings().setMyLocationButtonEnabled(true);
                GDNSharedPrefrences.getMap().setMyLocationEnabled(true);
                GDNSharedPrefrences.getMap().getUiSettings().setScrollGesturesEnabled(false);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 15);
                GDNSharedPrefrences.getMap().animateCamera(cameraUpdate);
            }

            //GDNSharedPrefrences.setCurrentState(GDNConstants.ONLINE);
            //sendAvailableRequesttoServer(GDNApiHelper.STATE_AVAILABLE);
            //onlineBtn.setText("GO OFFLINE");

        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }


    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(gcmBroadcastReceiver,
                new IntentFilter(GDNSharedPrefrences.REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(gcmBroadcastReceiver);
        super.onPause();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(MainActivity.this, "Google API Connection Failed!", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_online:
                changeState();
                break;
        }
    }

    private void changeState() {
        if(GDNSharedPrefrences.getCurrentState().equals(GDNConstants.ONLINE)){
            sendUnavailableRequesttoServer(GDNApiHelper.STATE_UNAVAILABLE);
        }else {
            sendAvailableRequesttoServer(GDNApiHelper.STATE_AVAILABLE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        GDNSharedPrefrences.setMap(googleMap);
        GDNSharedPrefrences.getMap().getUiSettings().setMyLocationButtonEnabled(true);
        GDNSharedPrefrences.getMap().setMyLocationEnabled(true);
        GDNSharedPrefrences.getMap().getUiSettings().setScrollGesturesEnabled(false);
        if (GDNSharedPrefrences.getLastLocation() != null) {
            Location lastLocation = GDNSharedPrefrences.getLastLocation();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 15);
            GDNSharedPrefrences.getMap().animateCamera(cameraUpdate);
        }
    }

    @NonNull
    private void sendAvailableRequesttoServer(String url) {
        JsonObjectRequest request =    new JsonObjectRequest
                (Request.Method.GET, url, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        GDNSharedPrefrences.setCurrentState(GDNConstants.ONLINE);
                        onlineBtn.setText("GO OFFLINE");
                        Toast.makeText(MainActivity.this, "You are Online!", Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.e("VolleyErorr", error.getLocalizedMessage() + error.getMessage());
                    }
                });
        GDNVolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    @NonNull
    private void sendUnavailableRequesttoServer(String url) {
        JsonObjectRequest request =     new JsonObjectRequest
                (Request.Method.GET, url, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        GDNSharedPrefrences.setCurrentState(GDNConstants.OFFLINE);
                        onlineBtn.setText("GO ONLINE");
                        Toast.makeText(MainActivity.this, "You are Offline", Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.e("VolleyErorr", error.getLocalizedMessage() + error.getMessage());
                    }
                });
        GDNVolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}
