package com.yogesh.groceryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdapterOrderedItem extends RecyclerView.Adapter<AdapterOrderedItem.HolderOrderedItem> {

    public ArrayList<ModelOrderedItem> orderedItemArrayList;
    private Context context;

    public AdapterOrderedItem(Context context, ArrayList<ModelOrderedItem> orderedItemArrayList) {
        this.context = context;
        this.orderedItemArrayList = orderedItemArrayList;
    }

    @NonNull
    @Override
    public HolderOrderedItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate Layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_ordereditem, parent, false);
        return new HolderOrderedItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderedItem holder, int position) {
        final ModelOrderedItem modelOrderedItem = orderedItemArrayList.get(position);
        String pId = modelOrderedItem.getpId();
        String name = modelOrderedItem.getName();
        final String cost = modelOrderedItem.getCost();
        String price = modelOrderedItem.getPrice();
        String quantity = modelOrderedItem.getQuantity();

        holder.itemTitleTextView.setText("" + name);
        holder.itemPriceTextView.setText("Total Price: Rs " + cost);
        holder.itemPriceEachTextView.setText("Rs " + price);
        holder.itemQuantityTextView.setText("[" + quantity + "]");
    }

    @Override
    public int getItemCount() {
        return orderedItemArrayList.size();
    }

    class HolderOrderedItem extends RecyclerView.ViewHolder {

        private TextView itemTitleTextView, itemPriceTextView, itemPriceEachTextView,
                itemQuantityTextView;

        public HolderOrderedItem(@NonNull View itemView) {
            super(itemView);

            itemTitleTextView = itemView.findViewById(R.id.itemTitleTextView);
            itemPriceTextView = itemView.findViewById(R.id.itemPriceTextView);
            itemPriceEachTextView = itemView.findViewById(R.id.itemPriceEachTextView);
            itemQuantityTextView = itemView.findViewById(R.id.itemQuantityTextView);
        }


    }
}
