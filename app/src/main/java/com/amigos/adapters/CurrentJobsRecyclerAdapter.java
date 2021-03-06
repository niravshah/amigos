package com.amigos.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amigos.R;
import com.amigos.activity.JobDetailsActivity;
import com.amigos.model.JobInfo;

import java.util.List;

/**
 * Created by Nirav on 21/11/2015.
 */
public class CurrentJobsRecyclerAdapter extends RecyclerView.Adapter<CurrentJobsRecyclerAdapter.ViewHolder> {

    private List<JobInfo> mItems;
    private Context context;
    public CurrentJobsRecyclerAdapter(Context ctx, List<JobInfo> items) {
        context = ctx; mItems = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.job_queue_list_row, viewGroup, false);
        return new ViewHolder(context,v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        JobInfo item = mItems.get(i);
        viewHolder.mJobItem.setText(item.getJobId());
        viewHolder.mJobItemstatus.setText(getJobStatus(item));
        viewHolder.jobInfo = item.getJobDetails();
        viewHolder.dropLat = item.getDropLat();
        viewHolder.dropLon = item.getDropLon();
        viewHolder.pickupLat = item.getPickupLat();
        viewHolder.pickupLon = item.getPickupLon();
        viewHolder.requesterId = item.getRequesterId();
    }

    private String getJobStatus(JobInfo item) {
        switch(item.getJobStatus()) {
            case "in_progress":
                return "IN PROGRESS";
            case "looking_for_drivers":
                return "SEARCHING DRIVERS";
            case "new":
                return "NEW";
            default:
                return item.getJobStatus();
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView mJobItem;
        private final TextView mJobItemstatus;
        private String jobInfo;
        private Context context;
        Double pickupLat;
        Double pickupLon;
        Double dropLat;
        Double dropLon;
        String requesterId;



        ViewHolder(Context ctx, View v) {
            super(v);
            mJobItem = (TextView)v.findViewById(R.id.job_item);
            mJobItemstatus = (TextView)v.findViewById(R.id.job_item_status);
            context = ctx;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, JobDetailsActivity.class);
            intent.putExtra("jobId", mJobItem.getText());
            intent.putExtra("details", jobInfo);

            intent.putExtra("pickupLatd", pickupLat);
            intent.putExtra("pickupLong", pickupLon);
            intent.putExtra("dropLatd", dropLat);
            intent.putExtra("dropLong", dropLon);
            intent.putExtra("requesterId", requesterId);

            context.startActivity(intent);
        }
    }

}
