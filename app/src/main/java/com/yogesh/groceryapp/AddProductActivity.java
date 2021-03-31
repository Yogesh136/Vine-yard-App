package com.yogesh.groceryapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddProductActivity extends AppCompatActivity {

    private CircularImageView productIconImageView;
    private EditText titleEditText, descriptionEditText, quantityEditText, priceEditText, discountPriceEditText, discountNoteEditText;
    private TextView categoryEditText;
    private SwitchCompat discountSwitch;
    private Button addProductButton;

    private Toolbar toolbar;

    //permission arrays
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int GALLERY_REQUEST_CODE = 300;


    //image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    //permissions array
    private String[] cameraPermissions;
    private String[] storagePermissions;


    //image uri
    private Uri image_uri;


    //firebase Auth
    private FirebaseAuth firebaseAuth;


    //progress
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);


        //initialize views
        productIconImageView = findViewById(R.id.productIconImageView);
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        categoryEditText = findViewById(R.id.categoryEditText);
        quantityEditText = findViewById(R.id.quantityEditText);
        priceEditText = findViewById(R.id.priceEditText);

        discountSwitch = findViewById(R.id.discountSwitch);

        discountPriceEditText = findViewById(R.id.discountPriceEditText);
        discountNoteEditText = findViewById(R.id.discountNoteEditText);

        addProductButton = findViewById(R.id.addProductButton);

        toolbar = (Toolbar) findViewById(R.id.addProductToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add a Product");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //unchecked, do not show discountPrice and discountNote
        discountPriceEditText.setVisibility(View.GONE);
        discountNoteEditText.setVisibility(View.GONE);


        //initialize permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        //if discountswitch is checked show discountPrice and discountNote else do not show
        discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //checked, show discountPrice and discountNote
                    discountPriceEditText.setVisibility(View.VISIBLE);
                    discountNoteEditText.setVisibility(View.VISIBLE);

                } else {
                    //unchecked, do not show discountPrice and discountNote
                    discountPriceEditText.setVisibility(View.GONE);
                    discountNoteEditText.setVisibility(View.GONE);
                }
            }
        });


        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);


        productIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //image pick dialog
                showImagePickDialog();
            }
        });


        categoryEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open Category dialog
                categoryDialog();
            }
        });


        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Steps
                //1. Input Data
                //2. Validate Data
                //3. Add data into database

                inputData();

            }

        });

    }


    private String productTitle, productDescription, productCategory, productQuantity, originalPrice, discountPrice, discountNote;
    private boolean discountAvailable = false;

    private void inputData() {
        //1. Input Data
        productTitle = titleEditText.getText().toString().trim();
        productDescription = descriptionEditText.getText().toString().trim();
        productCategory = categoryEditText.getText().toString().trim();
        productQuantity = quantityEditText.getText().toString().trim();
        originalPrice = priceEditText.getText().toString().trim();

        discountAvailable = discountSwitch.isChecked(); //true or false

        //2. Validate data
        if (TextUtils.isEmpty(productTitle)) {
            Toast.makeText(this, "Enter Product Title.", Toast.LENGTH_SHORT).show();
            return;
        }


        if (TextUtils.isEmpty(productCategory)) {
            Toast.makeText(this, "Select Product Category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(originalPrice)) {
            Toast.makeText(this, "Price is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(productQuantity)) {
            Toast.makeText(this, "Quantity is required.", Toast.LENGTH_SHORT).show();
            return;
        }


        if (discountAvailable) {
            //product is with discount
            discountPrice = discountPriceEditText.getText().toString().trim();
            discountNote = discountNoteEditText.getText().toString().trim();

            if (TextUtils.isEmpty(discountPrice)) {
                Toast.makeText(this, "Enter Discount Price.", Toast.LENGTH_SHORT).show();
                return;
            }

        } else {
            discountPrice = "0.0";
            discountNote = "";
        }


        addProduct();
    }

    private void addProduct() {
        //3. Add data into database
        progressDialog.setMessage("Adding product...");
        progressDialog.show();

        final String timeStamp = "" + System.currentTimeMillis();

        if (image_uri == null) {

            //adding product without image
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("productId", "" + timeStamp);
            hashMap.put("productTitle", "" + productTitle);
            hashMap.put("productDescription", "" + productDescription);
            hashMap.put("productCategory", "" + productCategory);
            hashMap.put("productQuantity", "" + productQuantity);
            hashMap.put("productIcon", ""); //no image provided
            hashMap.put("originalPrice", "" + originalPrice);
            hashMap.put("discountPrice", "" + discountPrice);
            hashMap.put("discountNote", "" + discountNote);
            hashMap.put("discountAvailable", "" + discountAvailable);
            hashMap.put("timeStamp", "" + timeStamp);
            hashMap.put("uid", "" + firebaseAuth.getUid());

            //Add datanow into FirebaseDatabase
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Products").child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //added to database
                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this, "Product has been successfully added...", Toast.LENGTH_SHORT).show();

                            //make ui clear again
                            clearData();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed adding to database
                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


        } else {
            //adding product with image

            //first upload image to storage
            String fileNameAndPath = "product_images/" + "" + timeStamp;

            StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileNameAndPath);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            //image has been uploaded

                            //get image url
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            Uri imagedownloadUri = uriTask.getResult();

                            if (uriTask.isSuccessful()) {
                                //url of image received, now upload to database

                                //adding product with image
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("productId", "" + timeStamp);
                                hashMap.put("productTitle", "" + productTitle);
                                hashMap.put("productDescription", "" + productDescription);
                                hashMap.put("productQuantity", "" + productQuantity);
                                hashMap.put("productCategory", "" + productCategory);
                                hashMap.put("productIcon", "" + imagedownloadUri); //no image provided
                                hashMap.put("originalPrice", "" + originalPrice);
                                hashMap.put("discountPrice", "" + discountPrice);
                                hashMap.put("discountNote", "" + discountNote);
                                hashMap.put("discountAvailable", "" + discountAvailable);
                                hashMap.put("timeStamp", "" + timeStamp);
                                hashMap.put("uid", "" + firebaseAuth.getUid());

                                //Add datanow into FirebaseDatabase
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(firebaseAuth.getUid()).child("Products").child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //added to database
                                                progressDialog.dismiss();
                                                Toast.makeText(AddProductActivity.this, "Product has been successfully added...", Toast.LENGTH_SHORT).show();

                                                //make ui clear again
                                                clearData();
                                            }

                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed adding to database
                                                progressDialog.dismiss();
                                                Toast.makeText(AddProductActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed uploading image
                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    private void clearData() {
        //clearing data after adding product
        titleEditText.setText("");
        descriptionEditText.setText("");
        categoryEditText.setText("");
        quantityEditText.setText("");
        priceEditText.setText("");

        discountPriceEditText.setText("");
        discountNoteEditText.setText("");

        productIconImageView.setImageResource(R.drawable.ic_shopping_kart);

        image_uri = null;

    }


    private void categoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Category");
        builder.setItems(Constants.productCategories, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //to show which category selected by the seller
                String category = Constants.productCategories[which];
                categoryEditText.setText(category);
            }
        }).show();

    }


    private void showImagePickDialog() {
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


    private void pickFromCamera() {
        //using mediastore to get highquality or original images
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image_Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image_Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }


    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }


    private boolean checkCameraPermissions() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return result && result1;
    }


    private boolean checkStoragePermissions() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return result;
    }


    private void requestCameraPermissions() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }


    private void requestStoragePermissions() {
        ActivityCompat.requestPermissions(this, storagePermissions, GALLERY_REQUEST_CODE);
    }


    //to handle permission requests
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickFromCamera();
                    } else {
                        //both or one permission is denied
                        Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case GALLERY_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        pickFromGallery();
                    } else {
                        //permission is denied
                        Toast.makeText(this, "Storage permission is required.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    //to handle pick results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {

            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                productIconImageView.setImageURI(image_uri);

            } else if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_uri = data.getData();
                productIconImageView.setImageURI(image_uri);
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