package com.yogesh.groceryapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductSeller> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct> productList, filterList;
    private FilterProduct filter;


    public AdapterProductSeller(Context context, ArrayList<ModelProduct> productList) {
        this.context = context;
        this.productList = productList;
        this.filterList = productList;
    }


    @NonNull
    @Override
    public HolderProductSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate Layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_seller, parent, false);
        return new HolderProductSeller(view);
    }


    @Override
    public void onBindViewHolder(@NonNull HolderProductSeller holder, int position) {

        //get data
        final ModelProduct modelProduct = productList.get(position);
        String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDescription();
        String icon = modelProduct.getProductIcon();
        String quantity = modelProduct.getProductQuantity();
        String title = modelProduct.getProductTitle();
        String timeStamp = modelProduct.getTimeStamp();
        String originalPrice = modelProduct.getOriginalPrice();


        //set data to views
        holder.titleTextView.setText(title);
        holder.quantityTextView.setText(quantity);
        holder.discountedNoteTextView.setText(discountNote);
        holder.discountPriceTextView.setText("Rs " + discountPrice);
        holder.originalPriceTextView.setText("Rs " + originalPrice);

        if (discountAvailable.equals("true")) {
            //product is on discount
            holder.discountPriceTextView.setVisibility(View.VISIBLE);
            holder.discountedNoteTextView.setVisibility(View.VISIBLE);

            holder.originalPriceTextView.setPaintFlags(holder.originalPriceTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //add Strike through on original Price
        } else {
            //product does not have discount
            holder.discountPriceTextView.setVisibility(View.GONE);
            holder.discountedNoteTextView.setVisibility(View.GONE);
            holder.originalPriceTextView.setPaintFlags(0);
        }


        //Product image
        try {
            Picasso.get().load(icon).placeholder(R.drawable.ic_quantity_grey).into(holder.productIconImageView);


        } catch (Exception e) {
            holder.productIconImageView.setImageResource(R.drawable.ic_quantity_grey);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //handle item clicks, show item details
                detailsBottomSheet(modelProduct); //model product contains details of clicked product
            }
        });

    }


    private void detailsBottomSheet(ModelProduct modelProduct) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.product_details_seller, null);
        bottomSheetDialog.setContentView(view);

        ImageButton backButton = view.findViewById(R.id.backButton);
        ImageButton deleteButton = view.findViewById(R.id.deleteButton);
        ImageButton editButton = view.findViewById(R.id.editButton);
        ImageView productIconImageView = view.findViewById(R.id.productIconImageView);
        TextView discountedNoteTextView = view.findViewById(R.id.discountedNoteTextView);
        TextView titleTextView = view.findViewById(R.id.titleTextView);
        TextView descriptionTextView = view.findViewById(R.id.descriptionTextView);
        TextView categoryTextView = view.findViewById(R.id.categoryTextView);
        TextView quantityTextView = view.findViewById(R.id.quantityTextView);
        TextView discountPriceTextView = view.findViewById(R.id.discountPriceTextView);
        TextView originalPriceTextView = view.findViewById(R.id.originalPriceTextView);

        //get data
        final String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDescription();
        String icon = modelProduct.getProductIcon();
        String quantity = modelProduct.getProductQuantity();
        final String title = modelProduct.getProductTitle();
        String timeStamp = modelProduct.getTimeStamp();
        String originalPrice = modelProduct.getOriginalPrice();

        //set data
        titleTextView.setText(title);
        descriptionTextView.setText(productDescription);
        categoryTextView.setText(productCategory);
        quantityTextView.setText(quantity);
        discountPriceTextView.setText("Rs " + discountPrice);
        discountedNoteTextView.setText(discountNote);
        originalPriceTextView.setText("Rs " + originalPrice);

        if (discountAvailable.equals("true")) {
            //product is on discount
            discountPriceTextView.setVisibility(View.VISIBLE);
            discountedNoteTextView.setVisibility(View.VISIBLE);

            originalPriceTextView.setPaintFlags(originalPriceTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //add Strike thorugh on original Price
        } else {
            //product does not have discount
            discountPriceTextView.setVisibility(View.GONE);
            discountedNoteTextView.setVisibility(View.GONE);

        }

        //Product image
        try {
            Picasso.get().load(icon).placeholder(R.color.colorGrey01).into(productIconImageView);


        } catch (Exception e) {
            productIconImageView.setImageResource(R.color.colorGrey01);
        }

        //show dialog
        bottomSheetDialog.show();

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(context, EditProductActivity.class);
                intent.putExtra("productId", id);
                context.startActivity(intent);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure you want to delete " + title + "?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //delete Product
                                deleteProduct(id);
                            }
                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });


    }


    private void deleteProduct(String id) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Product has been successfully removed.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return productList.size();
    }


    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterProduct(this, filterList);
        }
        return filter;
    }


    class HolderProductSeller extends RecyclerView.ViewHolder {

        private ImageView productIconImageView;
        private TextView discountedNoteTextView, titleTextView, quantityTextView,
                discountPriceTextView, originalPriceTextView;


        public HolderProductSeller(@NonNull View itemView) {
            super(itemView);

            productIconImageView = itemView.findViewById(R.id.productIconImageView);
            discountedNoteTextView = itemView.findViewById(R.id.discountedNoteTextView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            discountPriceTextView = itemView.findViewById(R.id.discountPriceTextView);
            originalPriceTextView = itemView.findViewById(R.id.originalPriceTextView);

        }
    }

}
