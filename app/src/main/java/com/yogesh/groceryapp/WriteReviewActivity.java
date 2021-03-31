package com.yogesh.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class WriteReviewActivity extends AppCompatActivity {

    private String shopUid;

    private CircularImageView profileImageView;
    private TextView shopNameTextView;
    private RatingBar ratingBar;
    private EditText reviewEditText;
    private Button submitButton;

    private FirebaseAuth firebaseAuth;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        shopUid = getIntent().getStringExtra("shopUid");

        toolbar = findViewById(R.id.writeReviewToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Write Review");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profileImageView = findViewById(R.id.profileImageView);
        shopNameTextView = findViewById(R.id.shopNameTextView);
        ratingBar = findViewById(R.id.ratingBar);
        reviewEditText = findViewById(R.id.reviewEditText);
        submitButton = findViewById(R.id.submitButton);

        firebaseAuth = FirebaseAuth.getInstance();

        loadShopInfo();

        loadMyReview();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });
    }

    private void loadShopInfo() {
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

    private void loadMyReview() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings").child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String uid = "" + snapshot.child("uid").getValue();
                            String ratings = "" + snapshot.child("ratings").getValue();
                            String review = "" + snapshot.child("review").getValue();
                            String timestamp = "" + snapshot.child("timestamp").getValue();

                            float myRating = Float.parseFloat(ratings);
                            ratingBar.setRating(myRating);
                            reviewEditText.setText(review);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void inputData() {
        String ratings = "" + ratingBar.getRating();
        String review = reviewEditText.getText().toString().trim();
        String timestamp = "" + System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", "" + firebaseAuth.getUid());
        hashMap.put("ratings", "" + ratings);
        hashMap.put("review", "" + review);
        hashMap.put("timestamp", "" + timestamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings").child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(WriteReviewActivity.this, "Thank you for your Feedback!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(WriteReviewActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}