package com.wetrip.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wetrip.R;
import com.wetrip.model.Trip;

import java.util.List;

public class TripsAdapter extends RecyclerView.Adapter<TripsAdapter.TripsViewHolder> {

    private List<Trip> trips;
    private int rowLayout;
    private Context context;


    public static class TripsViewHolder extends RecyclerView.ViewHolder {
        LinearLayout tripsLayout;
        TextView tripName;
        TextView distance;
        TextView members;
        TextView status;


        public TripsViewHolder(View v) {
            super(v);
            tripsLayout = (LinearLayout) v.findViewById(R.id.tripsLayout);
            tripName = (TextView) v.findViewById(R.id.tripName);
            distance = (TextView) v.findViewById(R.id.distance);
            members = (TextView) v.findViewById(R.id.members);
            status = (TextView) v.findViewById(R.id.status);
        }
    }

    public TripsAdapter(List<Trip> trips, int rowLayout, Context context) {
        this.trips = trips;
        this.rowLayout = rowLayout;
        this.context = context;
    }

    @Override
    public TripsAdapter.TripsViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new TripsViewHolder(view);
    }


    @Override
    public void onBindViewHolder(TripsViewHolder holder, final int position) {
        holder.tripName.setText(trips.get(position).getName());
        holder.distance.setText("0 Km");
        holder.members.setText("5 Members");
        holder.status.setText(trips.get(position).getStatus());
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }
}