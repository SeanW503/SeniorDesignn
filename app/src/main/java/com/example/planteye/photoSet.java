package com.example.planteye;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class photoSet extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1; // Request code for starting the camera activity
    private static final int PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_set);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button photoSet = findViewById(R.id.takePic);

        photoSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dispatchTakePictureIntent();
                } catch (Exception e) {
                    Toast.makeText(photoSet.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    // Method to start the camera activity
    private void dispatchTakePictureIntent() {
        if (checkPermission()) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            requestPermission();
        }
    }

    // Check permission
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    // Request for permission
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted) {
                        dispatchTakePictureIntent();
                    } else {
                        Toast.makeText(this, "Permission Denied, You cannot access camera.", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            ImageView imageView = findViewById(R.id.capturedImage);
            imageView.setImageBitmap(imageBitmap);
        }
    }
}
