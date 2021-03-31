package com.yogesh.groceryapp;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterProductUser extends RecyclerView.Adapter<AdapterProductUser.HolderProductUser> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct> productList, filterList;
    private FilterProductUser filter;

    public AdapterProductUser(Context context, ArrayList<ModelProduct> productList) {
        this.context = context;
        this.productList = productList;
        this.filterList = productList;
    }

    @NonNull
    @Override
    public HolderProductUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate Layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_user, parent, false);
        return new HolderProductUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductUser holder, int position) {
        //get data
        final ModelProduct modelProduct = productList.get(position);
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String originalPrice = modelProduct.getOriginalPrice();
        String productDescription = modelProduct.getProductDescription();
        String productTitle = modelProduct.getProductTitle();
        String productQuantity = modelProduct.getProductQuantity();
        String productId = modelProduct.getProductId();
        String timeStamp = modelProduct.getTimeStamp();
        String productIcon = modelProduct.getProductIcon();

        //set data to views
        holder.titleTextView.setText(productTitle);
        holder.discountedNoteTextView.setText(discountNote);
        holder.descriptionTextView.setText(productDescription);
        holder.originalPriceTextView.setText("Rs " + originalPrice);
        holder.discountPriceTextView.setText("Rs " + discountPrice);

        if (discountAvailable.equals("true")) {
            //product is on discount
            holder.discountPriceTextView.setVisibility(View.VISIBLE);
            holder.discountedNoteTextView.setVisibility(View.VISIBLE);

            holder.originalPriceTextView.setPaintFlags(holder.originalPriceTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //add Strike thorugh on original Price
        } else {
            //product does not have discount
            holder.discountPriceTextView.setVisibility(View.GONE);
            holder.discountedNoteTextView.setVisibility(View.GONE);
            holder.originalPriceTextView.setPaintFlags(0);
        }

        //Product image
        try {
            Picasso.get().load(productIcon).placeholder(R.drawable.ic_quantity_grey).into(holder.productIconImageView);


        } catch (Exception e) {
            holder.productIconImageView.setImageResource(R.drawable.ic_quantity_grey);
        }

        holder.addToCartTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                add product to cart
                showQuantityDialog(modelProduct);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                show product details
            }
        });


    }


    private double cost = 0;
    private double finalCost = 0;
    private int quantity = 0;

    private void showQuantityDialog(ModelProduct modelProduct) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity, null);

        ImageView productImageView = view.findViewById(R.id.productImageView);
        final TextView titleTextView = view.findViewById(R.id.titleTextView);
        TextView productQuantityTextView = view.findViewById(R.id.productQuantityTextView);
        TextView productDescriptionTextView = view.findViewById(R.id.productDescriptionTextView);
        TextView discountNoteTextView = view.findViewById(R.id.discountNoteTextView);
        final TextView originalPriceTextView = view.findViewById(R.id.originalPriceTextView);
        TextView priceDiscountedTextView = view.findViewById(R.id.priceDiscountedTextView);
        final TextView finalPriceTextView = view.findViewById(R.id.finalPriceTextView);
        ImageButton minusButton = view.findViewById(R.id.minusButton);
        final TextView quantityTextView = view.findViewById(R.id.quantityTextView);
        ImageButton plusButton = view.findViewById(R.id.plusButton);
        Button continueButton = view.findViewById(R.id.continueButton);


        final String productId = modelProduct.getProductId();
        String productTitle = modelProduct.getProductTitle();
        String productQuantity = modelProduct.getProductQuantity();
        String productDescription = modelProduct.getProductDescription();
        String discountNote = modelProduct.getDiscountNote();
        String productIcon = modelProduct.getProductIcon();

        final String price;
        if (modelProduct.getDiscountAvailable().equals("true")) {
            price = modelProduct.getDiscountPrice();
            discountNoteTextView.setVisibility(View.VISIBLE);
            originalPriceTextView.setPaintFlags(originalPriceTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //add Strike thorugh on original Price
        } else {
            discountNoteTextView.setVisibility(View.GONE);
            priceDiscountedTextView.setVisibility(View.GONE);
            price = modelProduct.getOriginalPrice();
        }

        cost = Double.parseDouble(price.replaceAll("Rs", ""));
        finalCost = Double.parseDouble(price.replaceAll("Rs", ""));
        quantity = 1;

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        try {
            Picasso.get().load(productIcon).placeholder(R.drawable.ic_cart_grey).into(productImageView);
        } catch (Exception e) {
            productImageView.setImageResource(R.drawable.ic_cart_grey);
        }

        titleTextView.setText("" + productTitle);
        productQuantityTextView.setText("" + productQuantity);
        productDescriptionTextView.setText("" + productDescription);
        discountNoteTextView.setText("" + discountNote);
        quantityTextView.setText("" + quantity);
        originalPriceTextView.setText("Rs " + modelProduct.getOriginalPrice());
        priceDiscountedTextView.setText("Rs " + modelProduct.getDiscountPrice());
        finalPriceTextView.setText("Rs " + finalCost);

        final AlertDialog dialog = builder.create();
        dialog.show();

//        increase quantity of product
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalCost = finalCost + cost;
                quantity++;

                finalPriceTextView.setText("Rs " + finalCost);
                quantityTextView.setText("" + quantity);
            }
        });

//        decrease quantity of product, ony if quantity is > 1
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 1) {
                    finalCost = finalCost - cost;
                    quantity--;

                    finalPriceTextView.setText("Rs " + finalCost);
                    quantityTextView.setText("" + quantity);
                }
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleTextView.getText().toString().trim();
                String priceEach = price;
                String totalPrice = finalPriceTextView.getText().toString().trim().replace("Rs", "");
                String quantity = quantityTextView.getText().toString().trim();

                //add to Sqlite
                addToCart(productId, title, priceEach, totalPrice, quantity);

                dialog.dismiss();
            }
        });

    }

    private int itemId = 1;

    private void addToCart(String productId, String title, String priceEach, String price, String quantity) {
        itemId++;

        EasyDB easyDB = EasyDB.init(context, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        Boolean b = easyDB.addData("Item_Id", itemId)
                .addData("Item_PID", productId)
                .addData("Item_Name", title)
                .addData("Item_Price_Each", priceEach)
                .addData("Item_Price", price)
                .addData("Item_Quantity", quantity)
                .doneDataAdding();

        Toast.makeText(context, "Added to Cart.", Toast.LENGTH_SHORT).show();

        ((ShopDetailsActivity)context).cartCount();
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterProductUser(this, filterList);
        }
        return filter;
    }

    class HolderProductUser extends RecyclerView.ViewHolder {
        /*
         * Holds views of recycler view
         * */

        private ImageView productIconImageView;
        private TextView discountedNoteTextView, titleTextView, descriptionTextView,
                addToCartTextView, originalPriceTextView, discountPriceTextView;


        public HolderProductUser(@NonNull View itemView) {
            super(itemView);

            productIconImageView = itemView.findViewById(R.id.productIconImageView);
            discountedNoteTextView = itemView.findViewById(R.id.discountedNoteTextView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            addToCartTextView = itemView.findViewById(R.id.addToCartTextView);
            discountPriceTextView = itemView.findViewById(R.id.discountPriceTextView);
            originalPriceTextView = itemView.findViewById(R.id.originalPriceTextView);
        }
    }
}
