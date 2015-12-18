package com.demandnow.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.demandnow.R;
import com.demandnow.activity.MainActivity;
import com.demandnow.helpers.GDNSharedPrefrences;
import com.demandnow.model.ServiceInfo;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Nirav on 28/11/2015.
 */
public class ServiceSelectorAdapter extends RecyclerView.Adapter<ServiceSelectorAdapter.ServiceViewHolder> {

    private List<ServiceInfo> contactList;
    private Context mContext;

    public ServiceSelectorAdapter(Context context, List<ServiceInfo> contactList) {
        this.mContext = context;
        this.contactList = contactList;
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    @Override
    public void onBindViewHolder(ServiceViewHolder serviceViewHolder, int i) {
        ServiceInfo ci = contactList.get(i);
        Picasso.with(mContext).load(ci.getServicePhotoUrl())
                .placeholder(R.drawable.ic_setting_light)
                .into(serviceViewHolder.imageView);
        serviceViewHolder.vName.setText(ci.getServiceName());
        serviceViewHolder.vDesc.setText(ci.getServiceDescription());
        serviceViewHolder.info = ci;
    }

    @Override
    public ServiceViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.cardv_service_selector, viewGroup, false);

        return new ServiceViewHolder(itemView);
    }

    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        protected TextView vName;
        protected TextView vDesc;
        protected ImageView imageView;
        protected ServiceInfo info;
        protected ImageButton iBtn;

        public ServiceViewHolder(View v) {
            super(v);
            vName = (TextView) v.findViewById(R.id.txtName);
            vDesc = (TextView) v.findViewById(R.id.txtDesc);
            imageView = (ImageView) v.findViewById(R.id.ic);
            iBtn = (ImageButton) v.findViewById(R.id.select_service);

            iBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GDNSharedPrefrences.setCurrentService(info.getServiceName());
                    GDNSharedPrefrences.setServiceId(info.getServiceId());
                    v.getContext().startActivity(new Intent(v.getContext(), MainActivity.class));
                }
            });

            v.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    GDNSharedPrefrences.setCurrentService(info.getServiceName());
                    GDNSharedPrefrences.setServiceId(info.getServiceId());
                    v.getContext().startActivity(new Intent(v.getContext(), MainActivity.class));
                }
            });
        }
    }
}
