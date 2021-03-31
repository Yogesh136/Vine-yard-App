package com.yogesh.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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

public class MainUserActivity extends AppCompatActivity {

    private TextView nameTextView, phoneTextView, tabShopsTextView, tabOrdersTextView;
    private ImageButton logoutButton;
    private RelativeLayout editProfileRl, shopRl, ordersRl;
    private CircularImageView profileImageview;


    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;


    private RecyclerView shopsRecyclerView, ordersRecyclerView;


    private ArrayList<ModelShop> shopList;
    private AdapterShop adapterShop;


    private ArrayList<ModelOrderUser> orderList;
    private AdapterOrderUser adapterOrderUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        nameTextView = findViewById(R.id.nameTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        tabShopsTextView = findViewById(R.id.tabShopsTextView);
        tabOrdersTextView = findViewById(R.id.tabOrdersTextView);

        logoutButton = findViewById(R.id.logoutButton);

        editProfileRl = findViewById(R.id.editProfileRl);
        shopRl = findViewById(R.id.shopRl);
        ordersRl = findViewById(R.id.ordersRl);

        shopsRecyclerView = findViewById(R.id.shopsRecyclerView);
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);

        profileImageview = findViewById(R.id.profileImageview);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);

        checkUser();


//        At the beginning show shops UI
        showShopsUI();


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
                startActivity(new Intent(MainUserActivity.this, ProfileEditUserActivity.class));

            }
        });


        tabShopsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //shows Shop UI
                showShopsUI();
            }
        });


        tabOrdersTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //shows orders UI
                showOrdersUI();
            }
        });
    }


    private void showShopsUI() {
        //show shops UI and hide orders UI
        shopRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabShopsTextView.setTextColor(getResources().getColor(R.color.colorBlack));
        tabShopsTextView.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTextView.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTextView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }


    private void showOrdersUI() {
        //show orders UI and hide shops UI
        shopRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);

        tabOrdersTextView.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrdersTextView.setBackgroundResource(R.drawable.shape_rect04);

        tabShopsTextView.setTextColor(getResources().getColor(R.color.colorWhite));
        tabShopsTextView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
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
                        Toast.makeText(MainUserActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(MainUserActivity.this, LoginActivity.class));
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
                            String name = "" + ds.child("name").getValue();
                            String email = "" + ds.child("email").getValue();
                            String phone = "" + ds.child("phone").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();
                            String accountType = "" + ds.child("accountType").getValue();
                            String city = "" + ds.child("city").getValue();

                            nameTextView.setText("Welcome " + name);
                            phoneTextView.setText("+91 " + phone);

                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(profileImageview);
                            } catch (Exception e) {
                                profileImageview.setImageResource(R.drawable.profile);
                            }

                            //load all shops which are in the users city
                            loadShops(city);

                            loadOrders();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void loadOrders() {
        orderList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String uid = "" + ds.getRef().getKey();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    ref.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            ModelOrderUser modelOrderUser = ds.getValue(ModelOrderUser.class);

                                            orderList.add(modelOrderUser);
                                        }
                                        adapterOrderUser = new AdapterOrderUser(MainUserActivity.this, orderList);
                                        ordersRecyclerView.setAdapter(adapterOrderUser);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void loadShops(final String myCity) {
        shopList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("accountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        shopList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelShop modelShop = ds.getValue(ModelShop.class);

                            String shopCity = "" + ds.child("city").getValue();

//                            show only city shops
                            if (shopCity.equals(myCity)) {
                                shopList.add(modelShop);
                            }
                        }

                        adapterShop = new AdapterShop(MainUserActivity.this, shopList);
                        shopsRecyclerView.setAdapter(adapterShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}