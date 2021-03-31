package com.yogesh.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {

    private ImageView shopImageview;
    private TextView shopNameTextView, phoneTextView, emailTextView, openCloseTextView,
            addressTextView, filteredProductsTextView, cartCountTextView;
    private ImageButton callButton, mapButton, filterProductButton, cartButton, reviewButton;
    private EditText searchProductEditText;
    private RecyclerView productsRecyclerView;

    private Toolbar toolbar;

    private String shopUid;
    private String myLatitude, myLongitude, myPhone;
    private String shopLatitude, shopLongitude, shopName, shopEmail, shopPhone, shopAddress;

    private FirebaseAuth firebaseAuth;

    private RatingBar ratingBar;

    private ProgressDialog progressDialog;

    private ArrayList<ModelProduct> productList;
    private ArrayList<ModelCartItem> cartItemList;

    private AdapterProductUser adapterProductUser;
    private AdapterCartItem adapterCartItem;

    private EasyDB easyDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        shopImageview = findViewById(R.id.shopImageview);
        shopNameTextView = findViewById(R.id.shopNameTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        emailTextView = findViewById(R.id.emailTextView);
        openCloseTextView = findViewById(R.id.openCloseTextView);
        addressTextView = findViewById(R.id.addressTextView);
        filteredProductsTextView = findViewById(R.id.filteredProductsTextView);
        cartCountTextView = findViewById(R.id.cartCountTextView);
        ratingBar = findViewById(R.id.ratingBar);

        callButton = findViewById(R.id.callButton);
        mapButton = findViewById(R.id.mapButton);
        filterProductButton = findViewById(R.id.filterProductButton);
        cartButton = findViewById(R.id.cartButton);
        reviewButton = findViewById(R.id.reviewButton);

        searchProductEditText = findViewById(R.id.searchProductEditText);
        productsRecyclerView = findViewById(R.id.productsRecyclerView);

        toolbar = findViewById(R.id.shopDetailsToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Shop Details");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);

        shopUid = getIntent().getStringExtra("shopUid");

        firebaseAuth = FirebaseAuth.getInstance();

        loadMyInfo();

        loadShopDetails();

        loadShopProducts();

        loadReviews();

        easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

//        Each shop has a different cart, so when user opens a different store a new cart is to be present
        //so delete cart when other shop is opened
        deleteCartData();

        cartCount();

        searchProductEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductUser.getFilter().filter(s);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhone();
            }
        });

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });

        cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCartDialog();
            }
        });

        filterProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Select Category:");
                builder.setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //get Selected Item
                        String selected = Constants.productCategories1[which];
                        filteredProductsTextView.setText(selected);
                        if (selected.equals("All")) {
                            //load all products
                            loadShopProducts();
                        } else {
                            adapterProductUser.getFilter().filter(selected);
                        }
                    }
                }).show();
            }
        });

        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShopDetailsActivity.this, ShopReviewsActivity.class);
                intent.putExtra("shopUid", shopUid);
                startActivity(intent);
            }
        });
    }

    private float ratingSum = 0;

    private void loadReviews() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings").
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        ratingSum = 0;

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            float rating = Float.parseFloat("" + ds.child("ratings").getValue());
                            ratingSum = ratingSum + rating;

                        }

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum / numberOfReviews;

                        ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private void deleteCartData() {
        easyDB.deleteAllDataFromTable();
    }


    public void cartCount() {
        int count = easyDB.getAllData().getCount();
        if (count <= 0) {
            cartCountTextView.setVisibility(View.GONE);
        } else {
            cartCountTextView.setVisibility(View.VISIBLE);
            cartCountTextView.setText("" + count);
        }
    }


    public double allTotalPrice = 0.00;
    //need to access these views in adapter so making it public
    public TextView sTotalTextView, dFeeTextView, allTotalTextView;

    private void showCartDialog() {

        cartItemList = new ArrayList<>();

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart, null);

        TextView shopNameTextView = view.findViewById(R.id.shopNameTextView);
        RecyclerView cartItemsRecyclerView = view.findViewById(R.id.cartItemsRecyclerView);
        sTotalTextView = view.findViewById(R.id.sTotalTextView);
        dFeeTextView = view.findViewById(R.id.dFeeTextView);
        allTotalTextView = view.findViewById(R.id.totalTextView);
        Button checkoutButton = view.findViewById(R.id.checkoutButton);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        shopNameTextView.setText(shopName);

        EasyDB easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        Cursor res = easyDB.getAllData();
        while (res.moveToNext()) {
            String id = res.getString(1);
            String pId = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(cost);

            ModelCartItem modelCartItem = new ModelCartItem("" + id
                    , "" + pId
                    , "" + name
                    , "" + price
                    , "" + cost
                    , "" + quantity
            );

            cartItemList.add(modelCartItem);
        }

        adapterCartItem = new AdapterCartItem(this, cartItemList);
        cartItemsRecyclerView.setAdapter(adapterCartItem);

        dFeeTextView.setText("Rs 75.00");
        sTotalTextView.setText("Rs " + String.format("%.2f", allTotalPrice));
        allTotalTextView.setText("Rs " + (allTotalPrice + Double.parseDouble("Rs75".replace("Rs", ""))));

        //show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        //reset price on dialog dismiss
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice = 0.00;
            }
        });

        //place order
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate address
                if (myLatitude.equals("") || myLatitude.equals("null") || myLongitude.equals("") || myLongitude.equals("null")) {
                    Toast.makeText(ShopDetailsActivity.this, "Provide address in profile to place orders.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (myPhone.equals("") || myPhone.equals("null")) {
                    Toast.makeText(ShopDetailsActivity.this, "Please update your phone number in your profile.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (cartItemList.size() == 0) {
                    Toast.makeText(ShopDetailsActivity.this, "No items in cart.", Toast.LENGTH_SHORT).show();
                    return;
                }

                submitOrder();
            }
        });
    }


    private void submitOrder() {
        progressDialog.setMessage("Placing Order...");
        progressDialog.show();

        final String timeStamp = "" + System.currentTimeMillis();
        String cost = allTotalTextView.getText().toString().trim().replace("Rs", "");

        final HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", "" + timeStamp);
        hashMap.put("orderTime", "" + timeStamp);
        hashMap.put("orderStatus", "In Progress");
        hashMap.put("orderCost", "" + cost);
        hashMap.put("orderBy", "" + firebaseAuth.getUid());
        hashMap.put("orderTo", "" + shopUid);
        hashMap.put("latitude", "" + myLatitude);
        hashMap.put("longitude", "" + myLongitude);

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //order info added now add order items
                        for (int i = 0; i < cartItemList.size(); i++) {
                            String pId = cartItemList.get(i).getpId();
                            String id = cartItemList.get(i).getId();
                            String cost = cartItemList.get(i).getCost();
                            String name = cartItemList.get(i).getName();
                            String price = cartItemList.get(i).getPrice();
                            String quantity = cartItemList.get(i).getQuantity();

                            HashMap<String, String> hashMap1 = new HashMap<>();
                            hashMap1.put("pId", pId);
                            hashMap1.put("name", name);
                            hashMap1.put("cost", cost);
                            hashMap1.put("price", price);
                            hashMap1.put("quantity", quantity);

                            ref.child(timeStamp).child("Items").child(pId).setValue(hashMap1);
                        }
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Successfully placed order.", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
                        intent.putExtra("orderTo", shopUid);
                        intent.putExtra("orderId", timeStamp);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void openMap() {
        String address = "https://maps.google.com/maps?saddr=" + myLatitude + "," + myLongitude + "&daddr=" + shopLatitude + "," + shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }


    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(shopPhone))));
        Toast.makeText(this, "" + shopPhone, Toast.LENGTH_SHORT).show();
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
                            myPhone = "" + ds.child("phone").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();
                            String accountType = "" + ds.child("accountType").getValue();
                            String city = "" + ds.child("city").getValue();
                            myLatitude = "" + ds.child("latitude").getValue();
                            myLongitude = "" + ds.child("longitude").getValue();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void loadShopDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get data from database
                String name = "" + snapshot.child("name").getValue();
                shopName = "" + snapshot.child("shopName").getValue();
                shopEmail = "" + snapshot.child("email").getValue();
                shopPhone = "" + snapshot.child("phone").getValue();
                shopLatitude = "" + snapshot.child("latitude").getValue();
                shopLongitude = "" + snapshot.child("longitude").getValue();
                shopAddress = "" + snapshot.child("address").getValue();
                String profileImage = "" + snapshot.child("profileImage").getValue();
                String shopOpen = "" + snapshot.child("shopOpen").getValue();

                //set value to database
                shopNameTextView.setText(shopName);
                emailTextView.setText(shopEmail);
                phoneTextView.setText("+91 " + shopPhone);
                addressTextView.setText(shopAddress);

                if (shopOpen.equals("true")) {
                    openCloseTextView.setText("Open");
                } else {
                    openCloseTextView.setText("Closed");
                }


                try {
                    Picasso.get().load(profileImage).placeholder(R.color.colorGrey02).into(shopImageview);
                } catch (Exception e) {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    private void loadShopProducts() {
        productList = new ArrayList<>();

        //get all products
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Products")
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
                        adapterProductUser = new AdapterProductUser(ShopDetailsActivity.this, productList);
                        //set Adapter
                        productsRecyclerView.setAdapter(adapterProductUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}