package com.yogesh.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopReviewsActivity extends AppCompatActivity {

    private String shopUid;

    private ImageView profileImageView;
    private TextView shopNameTextView, ratingsTextView;
    private RatingBar ratingBar;
    private RecyclerView reviewsRecyclerView;

    private Toolbar toolbar;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelReview> reviewArrayList;
    private AdapterReview adapterReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_reviews);

        shopUid = getIntent().getStringExtra("shopUid");

        toolbar = findViewById(R.id.shopReviewsToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Reviews");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        profileImageView = findViewById(R.id.profileImageView);
        shopNameTextView = findViewById(R.id.shopNameTextView);
        ratingsTextView = findViewById(R.id.ratingsTextView);
        ratingBar = findViewById(R.id.ratingBar);
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);

        firebaseAuth = FirebaseAuth.getInstance();

        loadShopDetails();

        loadReviews();
    }

    private float ratingSum = 0;

    private void loadReviews() {
        reviewArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings").
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        reviewArrayList.clear();
                        ratingSum = 0;

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            float rating = Float.parseFloat("" + ds.child("ratings").getValue());
                            ratingSum = ratingSum + rating;

                            ModelReview modelReview = ds.getValue(ModelReview.class);
                            reviewArrayList.add(modelReview);
                        }
                        //setup adapter
                        adapterReview = new AdapterReview(ShopReviewsActivity.this, reviewArrayList);
                        //set Adapter
                        reviewsRecyclerView.setAdapter(adapterReview);

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum / numberOfReviews;

                        ratingsTextView.setText(String.format("%.2f", avgRating) + " [" + numberOfReviews + "]");
                        ratingBar.setRating(avgRating);
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
                String shopName = "" + snapshot.child("shopName").getValue();
                String profileImage = "" + snapshot.child("profileImage").getValue();

                //set value to database
                shopNameTextView.setText(shopName);

                try {
                    Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_grey).into(profileImageView);
                } catch (Exception e) {
                    profileImageView.setImageResource(R.drawable.ic_store_grey);
                }
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