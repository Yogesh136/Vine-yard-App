package com.yogesh.groceryapp;

import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ProfileEditSellerActivity extends AppCompatActivity implements LocationListener {

    private CircularImageView profileImageview;
    private EditText nameEditText, shopNameEditText, phoneEditText, countryEditText, stateEditText, cityEditText,
            addressEditText;
    private Button updateButton;


    private SwitchCompat shopOpenSwitch, locationSwitch;

    private Toolbar toolbar;


    //permission constants
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;

    //image pick code
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 500;

    //permissions array
    private String[] cameraPermissions;
    private String[] locationPermissions;
    private String[] storagePermissions;

    //progress dialog
    private ProgressDialog progressDialog;

    //firebase Auth
    private FirebaseAuth firebaseAuth;

    //location Manager
    private LocationManager locationManager;

    //latitude and longitude
    private double latitude = 0.0, longitude = 0.0;

    //image uri
    private Uri image_uri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit_seller);

        profileImageview = findViewById(R.id.profileImageview);
        nameEditText = findViewById(R.id.nameEditText);
        shopNameEditText = findViewById(R.id.shopNameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        countryEditText = findViewById(R.id.countryEditText);
        stateEditText = findViewById(R.id.stateEditText);
        cityEditText = findViewById(R.id.cityEditText);
        addressEditText = findViewById(R.id.addressEditText);
        updateButton = findViewById(R.id.updateButton);
        shopOpenSwitch = findViewById(R.id.shopOpenSwitch);
        locationSwitch = findViewById(R.id.locationSwitch);

        toolbar = (Toolbar) findViewById(R.id.editProfileToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edit Profile");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //initialize permission arrays
        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //setup Progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);

        //initialize firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        //checkUser
        checkUser();


        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkLocationPermissions()) {
                    detectLocation();
                } else {
                    requestLocationPermissions();
                }
            }
        });

        profileImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePictureDialog();
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //update info
                inputData();
            }
        });
    }


    private String name, shopName, phone, country, city, state, address;
    private boolean shopOpen;

    private void inputData() {
        //input data
        name = nameEditText.getText().toString().trim();
        shopName = shopNameEditText.getText().toString().trim();
        phone = phoneEditText.getText().toString().trim();
        country = countryEditText.getText().toString().trim();
        city = cityEditText.getText().toString().trim();
        state = stateEditText.getText().toString().trim();
        address = addressEditText.getText().toString().trim();
        shopOpen = shopOpenSwitch.isChecked(); //true or false

        updateProfile();

    }


    private void updateProfile() {
        progressDialog.setMessage("Updating Profile...");
        progressDialog.show();

        if (image_uri == null) {
            //upload image without image

            //setup data to update
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("name", "" + name);
            hashMap.put("shopName", "" + shopName);
            hashMap.put("phone", "" + phone);
            hashMap.put("country", "" + country);
            hashMap.put("city", "" + city);
            hashMap.put("state", "" + state);
            hashMap.put("address", "" + address);
            hashMap.put("latitude", "" + latitude);
            hashMap.put("longitude", "" + longitude);
            hashMap.put("shopOpen", "" + shopOpen);


            //Update to database
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //data has been updated
                            progressDialog.dismiss();
                            Toast.makeText(ProfileEditSellerActivity.this, "Successfully updated your profile...", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //data has not been updated
                            progressDialog.dismiss();
                            Toast.makeText(ProfileEditSellerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


        } else {
            //upload with image

            //upload image First
            String filePathAndName = "profile_images/" + "" + firebaseAuth.getUid();
            //get storage reference
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uploaded, get url of uploaded image
                            Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                            while (!task.isSuccessful()) ;

                            Uri downloadImageUri = task.getResult();
                            if (task.isSuccessful()) {
                                //image_url received upload in database
                                //setup data to update
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("name", "" + name);
                                hashMap.put("shopName", "" + shopName);
                                hashMap.put("phone", "" + phone);
                                hashMap.put("country", "" + country);
                                hashMap.put("city", "" + city);
                                hashMap.put("state", "" + state);
                                hashMap.put("address", "" + address);
                                hashMap.put("latitude", "" + latitude);
                                hashMap.put("longitude", "" + longitude);
                                hashMap.put("shopOpen", "" + shopOpen);
                                hashMap.put("profileImage", "" + downloadImageUri);


                                //Update to database
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //data has been updated
                                                progressDialog.dismiss();
                                                Toast.makeText(ProfileEditSellerActivity.this, "Successfully updated your profile...", Toast.LENGTH_SHORT).show();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //data has not been updated
                                                progressDialog.dismiss();
                                                Toast.makeText(ProfileEditSellerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }
                        }

                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileEditSellerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


//
        }
    }


    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(ProfileEditSellerActivity.this, LoginActivity.class));
            finish();
        } else {
            loadMyInfo();
        }
    }


    private void loadMyInfo() {
        //load image, profile
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String accountType = "" + ds.child("accountType").getValue();
                            String address = "" + ds.child("address").getValue();
                            String city = "" + ds.child("city").getValue();
                            String state = "" + ds.child("state").getValue();
                            String country = "" + ds.child("country").getValue();
                            String email = "" + ds.child("email").getValue();
                            latitude = Double.parseDouble(" " + ds.child("latitude").getValue());
                            longitude = Double.parseDouble(" " + ds.child("longitude").getValue());
                            String name = "" + ds.child("name").getValue();
                            String online = "" + ds.child("online").getValue();
                            String phone = "" + ds.child("phone").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();
                            String timestamp = "" + ds.child("timestamp").getValue();
                            String shopName = "" + ds.child("shopName").getValue();
                            String shopOpen = "" + ds.child("shopOpen").getValue();
                            String uid = "" + ds.child("uid").getValue();

                            nameEditText.setText(name);
                            phoneEditText.setText(phone);
                            countryEditText.setText(country);
                            stateEditText.setText(state);
                            cityEditText.setText(city);
                            addressEditText.setText(address);
                            shopNameEditText.setText(shopName);


                            if (shopOpen.equals("true")) {
                                shopOpenSwitch.setChecked(true);
                            } else {
                                shopOpenSwitch.setChecked(false);
                            }


                            //picasso loading Image
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_grey).into(profileImageview);

                            } catch (Exception e) {
                                profileImageview.setImageResource(R.drawable.ic_store_grey);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void showImagePictureDialog() {
        //options
        String[] options = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick image from");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (checkCameraPermissions()) {
                        pickFromCamera();
                    } else {
                        requestCameraPermissions();
                    }

                } else {
                    if (checkStoragePermissions()) {
                        pickFromGallery();
                    } else {
                        requestStoragePermissions();
                    }
                }
            }
        }).show();
    }


    private boolean checkCameraPermissions() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return result && result1;
    }


    private void pickFromCamera() {
        //to pick image from gallery
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);

    }


    private void requestCameraPermissions() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }


    private boolean checkStoragePermissions() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        return result;
    }


    private void pickFromGallery() {
        //to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }


    private void requestStoragePermissions() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }


    private boolean checkLocationPermissions() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return result;
    }


    private void detectLocation() {
        Toast.makeText(this, "Please Wait...", Toast.LENGTH_SHORT).show();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_REQUEST_CODE);
    }


    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();
    }


    private void findAddress() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            String address = addresses.get(0).getAddressLine(0);
            String country = addresses.get(0).getCountryName();
            String state = addresses.get(0).getAdminArea();
            String city = addresses.get(0).getLocality();

            //set info into editText
            addressEditText.setText(address);
            countryEditText.setText(country);
            stateEditText.setText(state);
            cityEditText.setText(city);

        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }


    @Override
    public void onProviderEnabled(String provider) {

    }


    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Location permission is disabled...", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean locationAccepted = (grantResults[0]) == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted) {
                        detectLocation();
                    } else {
                        Toast.makeText(this, "Location permission is necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }

            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = (grantResults[0]) == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = (grantResults[1]) == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Camera permission is necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }


            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = (grantResults[0]) == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Storage permission is necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {

            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_uri = data.getData();
                profileImageview.setImageURI(image_uri);

            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
//                image_uri = data.getData();
                profileImageview.setImageURI(image_uri);
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}