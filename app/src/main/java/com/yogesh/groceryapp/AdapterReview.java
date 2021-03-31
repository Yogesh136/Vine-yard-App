package com.yogesh.groceryapp;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterReview extends RecyclerView.Adapter<AdapterReview.HolderReview> {

    private Context context;
    public ArrayList<ModelReview> reviewArrayList;

    public AdapterReview(Context context, ArrayList<ModelReview> reviewArrayList) {
        this.context = context;
        this.reviewArrayList = reviewArrayList;
    }

    @NonNull
    @Override
    public HolderReview onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_review, parent, false);
        return new HolderReview(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderReview holder, int position) {
        final ModelReview modelReview = reviewArrayList.get(position);
        String uid = modelReview.getUid();
        String ratings = modelReview.getRatings();
        String timestamp = modelReview.getTimestamp();
        String review = modelReview.getReview();


        loadUserDetail(modelReview, holder);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String formatedDate = DateFormat.format("dd/MM/yyyy", calendar).toString();


        holder.ratingBar.setRating(Float.parseFloat(ratings));
        holder.reviewTextView.setText(review);
        holder.dateTextView.setText(formatedDate);
    }

    private void loadUserDetail(ModelReview modelReview, final HolderReview holder) {
        String uid = modelReview.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = "" + snapshot.child("name").getValue();
                String profileImage = "" + snapshot.child("profileImage").getValue();

                holder.nameTextView.setText(name);

                try {
                    Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(holder.profileImageview);
                } catch (Exception e) {
                    holder.profileImageview.setImageResource(R.drawable.profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return reviewArrayList.size();
    }

    class HolderReview extends RecyclerView.ViewHolder {

        private ImageView profileImageview;
        private TextView nameTextView, reviewTextView, dateTextView;
        private RatingBar ratingBar;

        public HolderReview(@NonNull View itemView) {
            super(itemView);

            profileImageview = itemView.findViewById(R.id.profileImageview);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            reviewTextView = itemView.findViewById(R.id.reviewTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }
}
