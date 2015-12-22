package com.amigos.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.amigos.R;
import com.amigos.adapters.ServiceSelectorAdapter;
import com.amigos.helpers.GDNApiHelper;
import com.amigos.helpers.GDNVolleySingleton;
import com.amigos.model.ServiceInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nirav on 27/11/2015.
 */
public class ServiceSelectorActivity extends GDNBaseActivity {

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_selector);
        renderNavigationDrawer();
        renderChildActivityToolbar();

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        RecyclerView rv = (RecyclerView) findViewById(R.id.service_selector_rv);
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(llm);
        getContactList(rv);
    }


    private void getContactList(final RecyclerView rv) {

        JsonArrayRequest jsObjRequest = new JsonArrayRequest
                (Request.Method.GET, GDNApiHelper.SERVICES_URL, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        List<ServiceInfo> list = new ArrayList<>();
                        for(int i=0;i<response.length();i++){
                            JSONObject serviceInfoObj = null;
                            try {
                                serviceInfoObj = (JSONObject) response.get(i);
                                String serviceId = (String) serviceInfoObj.get("serviceId");
                                String serviceName = (String) serviceInfoObj.get("serviceName");
                                String serviceDescription = (String) serviceInfoObj.get("serviceDescription");
                                String servicePhoto = (String) serviceInfoObj.get("servicePhoto");
                                Boolean newService = (Boolean) serviceInfoObj.get("new");
                                Boolean active = (Boolean) serviceInfoObj.get("active");
                                list.add(new ServiceInfo(serviceId,serviceName,serviceDescription,servicePhoto,newService,active));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        progressBar.setVisibility(View.GONE);
                        ServiceSelectorAdapter adapter = new ServiceSelectorAdapter(ServiceSelectorActivity.this, list);
                        rv.setAdapter(adapter);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.e("VolleyErorr", error.getLocalizedMessage() + error.getMessage());
                    }
                });


        GDNVolleySingleton.getInstance(this).addToRequestQueue(jsObjRequest);


    }


}
