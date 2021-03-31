package com.yogesh.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class OrderDetailsSellerActivity extends AppCompatActivity {

    private String orderBy, orderId;

    private Toolbar toolbar;

    private TextView orderIdTextView, dateTextView, orderStatusTextView, nameTextView, phoneTextView,
            totalItemsTextView, amountTextView, addressTextView;
    private RecyclerView itemsRecyclerView;
    private ImageButton editButton, mapButton;

    private FirebaseAuth firebaseAuth;

    String sourceLatitude, sourceLongitude, destinationLatitude, destinationLongitude;

    private ArrayList<ModelOrderedItem> orderedItemArrayList;
    private AdapterOrderedItem adapterOrderedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_seller);

        Intent intent = getIntent();
        orderId = intent.getStringExtra("orderId");
        orderBy = intent.getStringExtra("orderBy");

        toolbar = findViewById(R.id.orderDetailsToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Order Details");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        orderIdTextView = findViewById(R.id.orderIdTextView);
        dateTextView = findViewById(R.id.dateTextView);
        orderStatusTextView = findViewById(R.id.orderStatusTextView);
        nameTextView = findViewById(R.id.nameTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        amountTextView = findViewById(R.id.amountTextView);
        addressTextView = findViewById(R.id.addressTextView);
        totalItemsTextView = findViewById(R.id.totalItemsTextView);

        editButton = findViewById(R.id.editButton);
        mapButton = findViewById(R.id.mapButton);

        itemsRecyclerView = findViewById(R.id.itemsRecyclerView);

        firebaseAuth = FirebaseAuth.getInstance();

        loadMyInfo();

        loadBuyerInfo();

        loadOrderDetails();

        loadOrderedItems();

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //edit progress
                editOrderStatusDialog();
            }
        });
    }

    private void editOrderStatusDialog() {
        //options to display in dialog
        final String[] options = {"In Progress", "Completed", "Cancelled"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Status").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle clicks
                String selectedOption = options[which];
                editOrderStatus(selectedOption);
            }
        }).show();
    }

    private void editOrderStatus(final String selectedOption) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("orderStatus", "" + selectedOption);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").child(orderId).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //added to database
                        Toast.makeText(OrderDetailsSellerActivity.this, "Order is now " + selectedOption, Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed adding to database
                        Toast.makeText(OrderDetailsSellerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        sourceLatitude = "" + snapshot.child("latitude").getValue();
                        sourceLongitude = "" + snapshot.child("longitude").getValue();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void loadBuyerInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        destinationLatitude = "" + snapshot.child("latitude").getValue();
                        destinationLongitude = "" + snapshot.child("longitude").getValue();
                        String name = "" + snapshot.child("name").getValue();
                        String phone = "" + snapshot.child("phone").getValue();

                        nameTextView.setText(name);
                        phoneTextView.setText("+91 " + phone);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void openMap() {
        String address = "https://maps.google.com/maps?saddr=" + sourceLatitude + "," + sourceLongitude + "&daddr=" + destinationLatitude + "," + destinationLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }


    private void loadOrderDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String orderBy = "" + snapshot.child("orderBy").getValue();
                        String orderCost = "" + snapshot.child("orderCost").getValue();
                        String orderId = "" + snapshot.child("orderId").getValue();
                        String orderStatus = "" + snapshot.child("orderStatus").getValue();
                        String orderTime = "" + snapshot.child("orderTime").getValue();
                        String orderTo = "" + snapshot.child("orderTo").getValue();
//                        String deliveryFee = ""+snapshot.child("deliveryFee").getValue();
                        String latitude = "" + snapshot.child("latitude").getValue();
                        String longitude = "" + snapshot.child("longitude").getValue();

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String formatedTime = DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString();


                        if (orderStatus.equals("In Progress")) {
                            orderStatusTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
                        } else if (orderStatus.equals("Completed")) {
                            orderStatusTextView.setTextColor(getResources().getColor(R.color.colorGreen));
                        } else if (orderStatus.equals("Cancelled")) {
                            orderStatusTextView.setTextColor(getResources().getColor(R.color.colorRed));
                        }

                        orderIdTextView.setText(orderId);
                        orderStatusTextView.setText(orderStatus);
                        dateTextView.setText(formatedTime);
                        amountTextView.setText("Rs " + orderCost);

                        findAddress(latitude, longitude);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void findAddress(String latitude, String longitude) {
        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);
            String address = addresses.get(0).getAddressLine(0); //complete address
            addressTextView.setText(address);

        } catch (Exception e) {

        }
    }


    private void loadOrderedItems() {
        orderedItemArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderedItemArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelOrderedItem modelOrderedItem = ds.getValue(ModelOrderedItem.class);

                            orderedItemArrayList.add(modelOrderedItem);
                        }

                        adapterOrderedItem = new AdapterOrderedItem(OrderDetailsSellerActivity.this, orderedItemArrayList);
                        itemsRecyclerView.setAdapter(adapterOrderedItem);

                        totalItemsTextView.setText("" + snapshot.getChildrenCount());
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