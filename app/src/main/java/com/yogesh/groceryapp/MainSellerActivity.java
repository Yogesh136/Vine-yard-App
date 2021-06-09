package com.yogesh.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainSellerActivity extends AppCompatActivity {

    private TextView nameTextView, shopNameTextView, phoneNumberTextView, tabProductsTextView, tabOrdersTextView, filteredProductsTextView, filteredOrdersTextView;
    private EditText searchProductEditText;
    private ImageButton logoutButton, addProductButton, filterProductButton, filterOrderButton, reviewsButton;
    private CircularImageView profileImageView;
    private RelativeLayout productsRelativeLayout, ordersRelativeLayout, editProfileRl;
    private RecyclerView productsRecyclerView, ordersRecyclerView;


    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelProduct> productList;
    private AdapterProductSeller adapterProductSeller;

    private ArrayList<ModelOrderShop> orderShopArrayList;
    private AdapterOrderShop adapterOrderShop;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);


        nameTextView = findViewById(R.id.nameTextView);
        shopNameTextView = findViewById(R.id.shopNameTextView);
        phoneNumberTextView = findViewById(R.id.phoneNumberTextView);
        tabProductsTextView = findViewById(R.id.tabProductsTextView);
        tabOrdersTextView = findViewById(R.id.tabOrdersTextView);
        filteredProductsTextView = findViewById(R.id.filteredProductsTextView);
        filteredOrdersTextView = findViewById(R.id.filteredOrdersTextView);


        searchProductEditText = findViewById(R.id.searchProductEditText);


        logoutButton = findViewById(R.id.logoutButton);
        addProductButton = findViewById(R.id.addProductButton);
        filterProductButton = findViewById(R.id.filterProductButton);
        filterOrderButton = findViewById(R.id.filterOrderButton);
        reviewsButton = findViewById(R.id.reviewsButton);


        productsRelativeLayout = findViewById(R.id.productsRelativeLayout);
        ordersRelativeLayout = findViewById(R.id.ordersRelativeLayout);
        editProfileRl = findViewById(R.id.editProfileRl);


        profileImageView = findViewById(R.id.profileImage);


        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);


        firebaseAuth = FirebaseAuth.getInstance();


        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);


        checkUser();


        loadAllProducts();


        loadAllOrders();


        showProductsUI();

        //search
        searchProductEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductSeller.getFilter().filter(s);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //make offline
                //sign out
                //go to login Activity

                makeMeOffline();
            }
        });


        editProfileRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open editProfile Activity
                startActivity(new Intent(MainSellerActivity.this, ProfileEditSellerActivity.class));
            }
        });


        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open add product Activity
                startActivity(new Intent(MainSellerActivity.this, AddProductActivity.class));
            }
        });


        tabProductsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load products

                showProductsUI();
            }
        });


        tabOrdersTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load orders

                showOrdersUI();
            }
        });


        filterProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Select Category:");
                builder.setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //get Selected Item
                        String selected = Constants.productCategories1[which];
                        filteredProductsTextView.setText(selected);
                        if (selected.equals("All")) {
                            //load all products
                            loadAllProducts();
                        } else {
                            loadFilteredProducts(selected);
                        }
                    }
                }).show();

            }
        });


        filterOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //options to display in dialog
                final String[] options = {"All", "In Progress", "Completed", "Cancelled"};
                //dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Show orders by").setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle clicks
                        if (which == 0) {
                            filteredOrdersTextView.setText("Showing All");
                            adapterOrderShop.getFilter().filter("");

                        } else {
                            String optionClicked = options[which];
                            filteredOrdersTextView.setText("Showing " + optionClicked + " Orders");
                            adapterOrderShop.getFilter().filter(optionClicked);
                        }

                    }
                }).show();
            }
        });


        reviewsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainSellerActivity.this, ShopReviewsActivity.class);
                intent.putExtra("shopUid", "" + firebaseAuth.getUid());
                startActivity(intent);
            }
        });
    }

    private void loadAllOrders() {
        orderShopArrayList = new ArrayList<>();

        //get all products
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        orderShopArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelOrderShop modelOrderShop = ds.getValue(ModelOrderShop.class);
                            orderShopArrayList.add(modelOrderShop);
                        }
                        //setup adapter
                        adapterOrderShop = new AdapterOrderShop(MainSellerActivity.this, orderShopArrayList);
                        ordersRecyclerView.setAdapter(adapterOrderShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadFilteredProducts(final String selected) {
        productList = new ArrayList<>();

        //get all products
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        productList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {

                            String productCategory = "" + ds.child("productCategory").getValue();

                            //if selected category matches product category then add in list
                            if (selected.equals(productCategory)) {
                                ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                                productList.add(modelProduct);
                            }
                        }
                        //setup adapter
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, productList);
                        //set Adapter
                        productsRecyclerView.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void loadAllProducts() {
        productList = new ArrayList<>();

        //get all products
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        productList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productList.add(modelProduct);
                        }
                        //setup adapter
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, productList);
                        productsRecyclerView.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }


    private void showProductsUI() {
        //show products UI and hide orders UI
        productsRelativeLayout.setVisibility(View.VISIBLE);
        ordersRelativeLayout.setVisibility(View.GONE);

        tabProductsTextView.setTextColor(getResources().getColor(R.color.colorBlack));
        tabProductsTextView.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTextView.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTextView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }


    private void showOrdersUI() {
        //show orders UI and hide products UI
        productsRelativeLayout.setVisibility(View.GONE);
        ordersRelativeLayout.setVisibility(View.VISIBLE);

        tabOrdersTextView.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrdersTextView.setBackgroundResource(R.drawable.shape_rect04);

        tabProductsTextView.setTextColor(getResources().getColor(R.color.colorWhite));
        tabProductsTextView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }


    private void makeMeOffline() {
        //after logging in make user online
        progressDialog.setMessage("Logging Out...");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "false");

        //update value to database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //update successful
                        progressDialog.dismiss();
                        firebaseAuth.signOut();
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to update
                        progressDialog.dismiss();
                        Toast.makeText(MainSellerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(MainSellerActivity.this, LoginActivity.class));
            finish();
        } else {
            loadMyInfo();
        }
    }


    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            //get data from database
                            String name = "" + ds.child("name").getValue();
                            String accountType = "" + ds.child("accountType").getValue();
                            String email = "" + ds.child("email").getValue();
                            String phoneNumber = "" + ds.child("phone").getValue();
                            String shopName = "" + ds.child("shopName").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();

                            //set value to database
                            nameTextView.setText("Welcome " + name);
                            shopNameTextView.setText(shopName);
                            phoneNumberTextView.setText("+91 " + phoneNumber);

                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_grey).into(profileImageView);
                            } catch (Exception e) {
                                profileImageView.setImageResource(R.drawable.ic_store_grey);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}