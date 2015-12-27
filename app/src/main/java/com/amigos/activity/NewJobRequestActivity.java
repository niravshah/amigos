package com.amigos.activity;

import android.app.ProgressDialog;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amigos.R;
import com.amigos.helpers.GDNApiHelper;
import com.amigos.helpers.GDNSharedPrefrences;
import com.amigos.helpers.GDNVolleySingleton;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.directions.route.Route;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Nirav on 22/12/2015.
 */
public class NewJobRequestActivity extends GDNBaseActivity implements View.OnClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, RoutingListener {

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation = GDNSharedPrefrences.getLastLocation();
    private ProgressDialog progressDialog;

    String address;
    String jobId;
    String requesterId;
    LatLng drop;
    LatLng pick;

    TextView routeInfo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_job_request);
        renderNavigationDrawer();
        renderChildActivityToolbar();

        Bundle extras = getIntent().getExtras();
        String jobDetails = extras.getString("details");
        String[] parts = jobDetails.split(":");

        requesterId = parts[0];
        jobId = parts[1];
        Double pickupLat = Double.valueOf(parts[2]);
        Double pickupLon = Double.valueOf(parts[3]);
        Double dropLat = Double.valueOf(parts[4]);
        Double dropLon = Double.valueOf(parts[5]);
        address = parts[6];

        drop = new LatLng(dropLat,dropLon);
        pick = new LatLng(pickupLat,pickupLon);

        buildGoogleApiClient();

        findViewById(R.id.accept_request).setOnClickListener(this);
        findViewById(R.id.reject_request).setOnClickListener(this);
        routeInfo = (TextView) findViewById(R.id.route_info);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.new_request_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        GDNSharedPrefrences.setMap(googleMap);
        GDNSharedPrefrences.getMap().getUiSettings().setMyLocationButtonEnabled(true);
        GDNSharedPrefrences.getMap().setMyLocationEnabled(true);
        GDNSharedPrefrences.getMap().getUiSettings().setScrollGesturesEnabled(true);

        if (GDNSharedPrefrences.getLastLocation() != null) {
            Location lastLocation = GDNSharedPrefrences.getLastLocation();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 15);
            GDNSharedPrefrences.getMap().animateCamera(cameraUpdate);
        }

        GDNSharedPrefrences.getMap().setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition arg0) {
                addPickupDropMarkers();
                // Remove listener to prevent position reset on camera move.
                GDNSharedPrefrences.getMap().setOnCameraChangeListener(null);
            }
        });


    }

    private void addPickupDropMarkers() {

        LatLng curPos = null;
        GDNSharedPrefrences.getMap().addMarker(new MarkerOptions().position(drop).title("Drop"));
        GDNSharedPrefrences.getMap().addMarker(new MarkerOptions().position(pick).title("Pickup"));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(drop);
        builder.include(pick);
        if (GDNSharedPrefrences.getLastLocation() != null) {
            curPos = new LatLng(GDNSharedPrefrences.getLastLocation().getLatitude(), GDNSharedPrefrences.getLastLocation().getLongitude());
            builder.include(curPos);
            GDNSharedPrefrences.getMap().addMarker(new MarkerOptions().position(curPos).title("You"));
        }

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(builder.build(), 16);
        GDNSharedPrefrences.getMap().animateCamera(cu);
        Routing routing;

        if(curPos != null){
            routing = new Routing.Builder()
                    .travelMode(Routing.TravelMode.DRIVING)
                    .withListener(this)
                    .waypoints(curPos,pick, drop)
                    .build();
        }else {
            routing = new Routing.Builder()
                    .travelMode(Routing.TravelMode.DRIVING)
                    .withListener(this)
                    .waypoints(pick, drop)
                    .build();
        }
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Fetching route information.", true);
        routing.execute();
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
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onRoutingFailure() {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        progressDialog.dismiss();
        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
        GDNSharedPrefrences.getMap().addPolyline(polyOptions);
        routeInfo.setText("Distance - " + route.get(shortestRouteIndex).getDistanceText()+": Duration - "+ route.get(shortestRouteIndex).getDurationText());
    }

    @Override
    public void onRoutingCancelled() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.accept_request:
                acceptRequest();
                break;
            case R.id.reject_request:
                rejectRequest();
                break;
        }
    }

    private void rejectRequest() {
        String url = GDNApiHelper.JOB_REQUEST + jobId  + "/" + requesterId + "/reject";
        JsonObjectRequest request =    new JsonObjectRequest
                (Request.Method.GET,url , new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getApplicationContext(), "Job Request Rejected", Toast.LENGTH_LONG).show();
                        NavUtils.navigateUpFromSameTask(NewJobRequestActivity.this);
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

    private void acceptRequest() {
        String url = GDNApiHelper.JOB_REQUEST + jobId + "/" + requesterId + "/accept";
        JsonObjectRequest request =    new JsonObjectRequest
                (Request.Method.GET,url , new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getApplicationContext(), "Job Request Accepted", Toast.LENGTH_LONG).show();
                        NavUtils.navigateUpFromSameTask(NewJobRequestActivity.this);
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
