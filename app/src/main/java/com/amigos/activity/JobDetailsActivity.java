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
public class JobDetailsActivity extends GDNBaseActivity implements View.OnClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, RoutingListener {

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
        setContentView(R.layout.activity_job_details);
        renderNavigationDrawer();
        renderChildActivityToolbar();

        Bundle extras = getIntent().getExtras();

        requesterId =  extras.getString("requesterId");
        jobId = extras.getString("jobId");
        Double pickupLat = extras.getDouble("pickupLatd");
        Double pickupLon = extras.getDouble("pickupLong");
        Double dropLat = extras.getDouble("dropLatd");
        Double dropLon = extras.getDouble("dropLong");


        drop = new LatLng(dropLat,dropLon);
        pick = new LatLng(pickupLat,pickupLon);

        buildGoogleApiClient();

        findViewById(R.id.request_complete).setOnClickListener(this);
        routeInfo = (TextView) findViewById(R.id.route_info);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.job_details_map);
        mapFragment.getMapAsync(this);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
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

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            GDNSharedPrefrences.setLastLocation(mLastLocation);
            setupMap();
        }
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
    public void onConnectionSuspended(int i) {

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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.request_complete:
                requestComplete();
                break;
        }
    }

    private void requestComplete() {
        String url = GDNApiHelper.JOB_REQUEST +jobId  + "/" + requesterId + "/complete";
        JsonObjectRequest request =    new JsonObjectRequest
                (Request.Method.GET,url , new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getApplicationContext(), "Job Marked Complete", Toast.LENGTH_LONG).show();
                        NavUtils.navigateUpFromSameTask(JobDetailsActivity.this);
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

    @Override
    public void onRoutingCancelled() {

    }
}
