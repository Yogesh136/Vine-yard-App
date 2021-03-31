package com.yogesh.groceryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HolderCartItem>{

    private Context context;
    public ArrayList<ModelCartItem> cartItems;

    public AdapterCartItem(Context context, ArrayList<ModelCartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;

    }

    @NonNull
    @Override
    public HolderCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate Layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_cartitem, parent, false);
        return new HolderCartItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCartItem holder, final int position) {

        final ModelCartItem modelCartItem = cartItems.get(position);
        final String id = modelCartItem.getId();
        String pId = modelCartItem.getpId();
        String name = modelCartItem.getName();
        final String cost = modelCartItem.getCost();
        String price = modelCartItem.getPrice();
        String quantity = modelCartItem.getQuantity();

        holder.itemTitleTextView.setText("" + name);
        holder.itemPriceTextView.setText("Rs "+ cost);
        holder.itemQuantityTextView.setText("[" + quantity + "]");
        holder.itemPriceEachTextView.setText("" + price);

        holder.itemRemoveTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //will create database if it does not exist, but in that case it must exist
                EasyDB easyDB = EasyDB.init(context, "ITEMS_DB")
                        .setTableName("ITEMS_TABLE")
                        .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                        .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                        .doneTableColumn();

                easyDB.deleteRow(1, id);
                Toast.makeText(context, "Removed from cart...", Toast.LENGTH_SHORT).show();

                cartItems.remove(position);
                notifyItemChanged(position);
                notifyDataSetChanged();

                double tx = Double.parseDouble((((ShopDetailsActivity)context).allTotalTextView.getText().toString().trim().replace("Rs", "")));
                double totalPrice = tx - Double.parseDouble(cost.replace("Rs", ""));
                double deliveryFee = Double.parseDouble((((ShopDetailsActivity)context).dFeeTextView.getText().toString().trim().replace("Rs", "")));
                double sTotalPrice = Double.parseDouble(String.format("%.2f",totalPrice)) - Double.parseDouble(String.format("%.2f",deliveryFee));
                ((ShopDetailsActivity)context).allTotalPrice = 0.00;
                ((ShopDetailsActivity)context).sTotalTextView.setText("Rs " + String.format("%.2f",sTotalPrice));
                ((ShopDetailsActivity)context).allTotalTextView.setText("Rs " + String.format("%.2f",Double.parseDouble(String.format("%.2f", totalPrice))));

                ((ShopDetailsActivity)context).cartCount();

            }
        });

    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class HolderCartItem extends RecyclerView.ViewHolder{

        private TextView itemTitleTextView, itemPriceTextView, itemPriceEachTextView,
                itemQuantityTextView,itemRemoveTextView;

        public HolderCartItem(@NonNull View itemView) {
            super(itemView);

            itemTitleTextView = itemView.findViewById(R.id.itemTitleTextView);
            itemPriceTextView = itemView.findViewById(R.id.itemPriceTextView);
            itemPriceEachTextView = itemView.findViewById(R.id.itemPriceEachTextView);
            itemQuantityTextView = itemView.findViewById(R.id.itemQuantityTextView);
            itemRemoveTextView = itemView.findViewById(R.id.itemRemoveTextView);
        }
    }
}
