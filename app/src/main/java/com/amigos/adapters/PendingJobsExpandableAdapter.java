package com.amigos.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.amigos.R;
import com.amigos.helpers.GDNVolleySingleton;
import com.amigos.model.JobInfo;
import com.amigos.model.ParentJobInfo;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentObject;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;

import java.util.List;

/**
 * Created by Nirav on 16/12/2015.
 */
public class PendingJobsExpandableAdapter extends ExpandableRecyclerAdapter<PendingJobsExpandableAdapter.JobsParentViewHolder, PendingJobsExpandableAdapter.JobsChildViewHolder> {

    private final LayoutInflater mInflater;
    private final Context ctx;

    public PendingJobsExpandableAdapter(Context context, List<ParentObject> parentItemList) {
        super(context, parentItemList);
        ctx = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public JobsParentViewHolder onCreateParentViewHolder(ViewGroup viewGroup) {
        View view = mInflater.inflate(R.layout.pending_jobs_parent, viewGroup, false);
        return new JobsParentViewHolder(view);
    }

    @Override
    public JobsChildViewHolder onCreateChildViewHolder(ViewGroup viewGroup) {
        View view = mInflater.inflate(R.layout.pending_jobs_child, viewGroup, false);
        return new JobsChildViewHolder(view);
    }

    @Override
    public void onBindParentViewHolder(final JobsParentViewHolder jobsParentViewHolder, int i, Object o) {
        ParentJobInfo info = (ParentJobInfo) o;
        jobsParentViewHolder.mCrimeTitleTextView.setText(info.getRname());
        jobsParentViewHolder.mTotal.setText("£" + info.getChildObjectList().size() * 4);
        jobsParentViewHolder.pInfo = info;
        ImageRequest request = new ImageRequest(info.getRphoto(),
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        jobsParentViewHolder.mImageView.setImageBitmap(bitmap);
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        jobsParentViewHolder.mImageView.setImageResource(R.drawable.profile);
                    }
                });

        GDNVolleySingleton.getInstance(ctx).addToRequestQueue(request);
    }

    @Override
    public void onBindChildViewHolder(JobsChildViewHolder jobsChildViewHolder, int i, Object o) {
        JobInfo job = (JobInfo) o;
        jobsChildViewHolder.mCrimeDateText.setText(job.getJobId());

    }

    public class JobsParentViewHolder extends ParentViewHolder {

        public TextView mCrimeTitleTextView;
        public ImageButton mParentDropDownArrow;
        public ImageView mImageView;
        public ParentJobInfo pInfo;
        public TextView mTotal;

        public JobsParentViewHolder(View itemView) {
            super(itemView);
            mCrimeTitleTextView = (TextView) itemView.findViewById(R.id.parent_list_item_crime_title_text_view);
            mParentDropDownArrow = (ImageButton) itemView.findViewById(R.id.parent_list_item_expand_arrow);
            mTotal = (TextView) itemView.findViewById(R.id.parent_list_item_total_amount);
            mImageView = (ImageView) itemView.findViewById(R.id.imageView2);
        }
    }

    public class JobsChildViewHolder extends ChildViewHolder {

        public TextView mCrimeDateText;
        public CheckBox mCrimeSolvedCheckBox;

        public JobsChildViewHolder(View itemView) {
            super(itemView);

            mCrimeDateText = (TextView) itemView.findViewById(R.id.child_list_item_crime_date_text_view);
            mCrimeSolvedCheckBox = (CheckBox) itemView.findViewById(R.id.child_list_item_crime_solved_check_box);
        }
    }
}