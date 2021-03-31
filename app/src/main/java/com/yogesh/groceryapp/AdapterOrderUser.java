package com.yogesh.groceryapp;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderUser extends RecyclerView.Adapter<AdapterOrderUser.HolderOrderUser> {

    private Context context;
    public ArrayList<ModelOrderUser> orderUserList;

    public AdapterOrderUser(Context context, ArrayList<ModelOrderUser> orderUserList) {
        this.context = context;
        this.orderUserList = orderUserList;
    }

    @NonNull
    @Override
    public HolderOrderUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate Layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_user, parent, false);
        return new HolderOrderUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderUser holder, int position) {
        ModelOrderUser modelOrderUser = orderUserList.get(position);
        final String orderId = modelOrderUser.getOrderId();
        String orderBy = modelOrderUser.getOrderBy();
        String orderCost = modelOrderUser.getOrderCost();
        String orderStatus = modelOrderUser.getOrderStatus();
        String orderTime = modelOrderUser.getOrderTime();
        final String orderTo = modelOrderUser.getOrderTo();

        //loadShop Info
        loadShopInfo(modelOrderUser, holder);

        holder.amountTextView.setText("Total Price: Rs " + orderCost);
        holder.statusTextView.setText(orderStatus);
        holder.orderIdTextView.setText("Order No: " + orderId);

        if (orderStatus.equals("In Progress")) {
            holder.statusTextView.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        } else if (orderStatus.equals("Completed")) {
            holder.statusTextView.setTextColor(context.getResources().getColor(R.color.colorGreen));
        } else if (orderStatus.equals("Cancelled")) {
            holder.statusTextView.setTextColor(context.getResources().getColor(R.color.colorRed));
        }


        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatedDate = DateFormat.format("dd/MM/yyyy", calendar).toString();

        holder.dateTextView.setText(formatedDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OrderDetailsUsersActivity.class);
                intent.putExtra("orderTo", orderTo);
                intent.putExtra("orderId", orderId);
                context.startActivity(intent);

            }
        });
    }

    private void loadShopInfo(ModelOrderUser modelOrderUser, final HolderOrderUser holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(modelOrderUser.getOrderTo())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = "" + snapshot.child("shopName").getValue();
                        holder.shopNameTextView.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return orderUserList.size();
    }

    class HolderOrderUser extends RecyclerView.ViewHolder {

        private TextView orderIdTextView, dateTextView, shopNameTextView,
                amountTextView, statusTextView;

        public HolderOrderUser(@NonNull View itemView) {
            super(itemView);

            orderIdTextView = itemView.findViewById(R.id.orderIdTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            shopNameTextView = itemView.findViewById(R.id.shopNameTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
        }
    }
}
