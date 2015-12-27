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

import com.amigos.R;
import com.amigos.adapters.CurrentJobsRecyclerAdapter;
import com.amigos.helpers.GDNApiHelper;
import com.amigos.helpers.GDNVolleySingleton;
import com.amigos.model.JobInfo;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Nirav on 16/12/2015.
 */
public class PendingJobsTabFragment extends Fragment {

    private static final String TAB_POSITION = "tab_position";
    public static final String TAB_NAME = "PAST";
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private Boolean swipeRefresh = false;


    public PendingJobsTabFragment() {

    }

    public static PendingJobsTabFragment newInstance(int tabPosition) {
        PendingJobsTabFragment fragment = new PendingJobsTabFragment();
        Bundle args = new Bundle();
        args.putInt(TAB_POSITION, tabPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.rv_pending_job_queue, container, false);
        recyclerView = (RecyclerView)v.findViewById(R.id.recyclerview_pending);
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

        String url = GDNApiHelper.JOBS_URL + "/all";
        JsonArrayRequest jsObjRequest = new JsonArrayRequest
                (Request.Method.GET, url, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        ArrayList<JobInfo> jobInfos = new ArrayList<>();
                        for(int i=0;i<response.length();i++){
                            try {
                                JSONObject obj = (JSONObject) response.get(i);
                                JobInfo info = new JobInfo(obj.getString("jobId"), obj.getString("currentStatus"));
                                info.setDropLat(obj.getDouble("dropLatd"));
                                info.setDropLon(obj.getDouble("dropLong"));
                                info.setPickupLat(obj.getDouble("pickupLatd"));
                                info.setPickupLon(obj.getDouble("pickupLong"));
                                info.setRequesterId(obj.getString("requesterId"));
                                jobInfos.add(info);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        recyclerView.setAdapter(new CurrentJobsRecyclerAdapter(getContext(),jobInfos));
                        if(swipeRefresh){
                            swipeContainer.setRefreshing(false);
                            //Toast.makeText(getActivity(), "Swipe Refresh", Toast.LENGTH_LONG).show();
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
