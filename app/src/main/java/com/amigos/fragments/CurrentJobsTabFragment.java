package com.amigos.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.amigos.R;
import com.amigos.adapters.CurrentJobsRecyclerAdapter;
import com.amigos.helpers.GDNApiHelper;
import com.amigos.helpers.GDNVolleySingleton;
import com.amigos.model.JobInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Nirav on 20/11/2015.
 */
public class CurrentJobsTabFragment extends Fragment {
    private static final String TAB_POSITION = "tab_position";
    public static final String TAB_NAME = "Current";
    private SwipeRefreshLayout swipeContainer;
    private Boolean swipeRefresh = false;

    public CurrentJobsTabFragment() {

    }

    public static CurrentJobsTabFragment newInstance(int tabPosition) {
        CurrentJobsTabFragment fragment = new CurrentJobsTabFragment();
        Bundle args = new Bundle();
        args.putInt(TAB_POSITION, tabPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.rv_current_job_queue, container, false);
        final RecyclerView recyclerView = (RecyclerView)v.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh = true;
                getCurrentJobQueueFromServer(recyclerView);
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        getCurrentJobQueueFromServer(recyclerView);
        return v;
    }

    private void getCurrentJobQueueFromServer(final RecyclerView recyclerView) {

        String url = GDNApiHelper.JOBS_URL;
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Iterator<String> respIterator = response.keys();
                        ArrayList<JobInfo> jobInfos = new ArrayList<>();
                        while(respIterator.hasNext()){
                            String next = respIterator.next();
                            try {
                                JSONArray arr = (JSONArray) response.get(next);
                                jobInfos.add(new JobInfo(next,arr.getString(1)));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                        recyclerView.setAdapter(new CurrentJobsRecyclerAdapter(getContext(),jobInfos));
                        if(swipeRefresh){
                            swipeContainer.setRefreshing(false);
                            Toast.makeText(getActivity(), "Swipe Refresh", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.e("VolleyErorr", "getAddressFromAddress - " + error.getLocalizedMessage() + error.getMessage());
                    }
                });

        GDNVolleySingleton.getInstance(getContext()).addToRequestQueue(jsObjRequest);
    }


}



