package com.example.planteye;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

//Chatgpt imports for camera2 api
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
//end of imports for camera2 api

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import java.util.List;


public class settings extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1; // Request code for starting the camera activity

    private static final int PERMISSION_REQUEST_CODE = 200;
    public static  final String EXTRA_IMAGE_PATH = "com.example.planteye.IMAGE_PATH";
    private static final int REQUEST_CODE = 1;
    private String currentPhotoPath;

    private EditText editText;
    private Button sendButton;
    private Button finishCaptureButton;  // <-- Declare the variable here
    private String sessionID;

    private RequestQueue requestQueue;


    private ArrayList<String> imagePaths = new ArrayList<>(); // <-- Add this line
    private LinearLayout imageContainer; // <-- Add this line
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        editText = findViewById(R.id.editText);
        sendButton = findViewById(R.id.sendButton);
        imageContainer = findViewById(R.id.imageContainer); // Assuming you have this LinearLayout in your XML

        requestQueue = Volley.newRequestQueue(this);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sessionID == null) {
                    sessionID = "Session_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                }
                String inputValue = editText.getText().toString();
                int intValue = Integer.parseInt(inputValue);
                sendHttpRequest("http://192.168.4.1/", intValue);
                //dispatchTakePictureIntent();
            }
        });

        finishCaptureButton = findViewById(R.id.finishCaptureButton);  // Initialize the button

        finishCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When the button is clicked, move to GalleryActivity
                Intent intent = new Intent(settings.this, GalleryActivity.class);
                intent.putStringArrayListExtra("imagePaths", imagePaths);
                intent.putExtra("SESSION_ID", sessionID);  // Pass the session ID to the next Activity
                startActivity(intent);
            }
        });


    }

    private void sendHttpRequest(String url, final int value) {
        try {
            JSONObject jsonParams = new JSONObject();
            jsonParams.put("value", value);

            final String requestBody = jsonParams.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Handle the response here (if needed)
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Handle any errors that occur during the request
                            error.printStackTrace();
                        }
                    }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        return null;
                    }
                }
            };

            // Add the request to the request queue
            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



//After this point is the camera/picture taking code as of 9/18/2023 we are trying to implement Camera2 api to replace the old depreciated camera api




/*
    // Method to start the camera activity
    private void dispatchTakePictureIntent() {
        if (checkPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(settings.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(settings.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                } else {
                    // Permission already granted; you can proceed with taking pictures.


                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();

        return image;
    }


    // Check permission
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    // Request for permission
    private void requestPermission() {
        ActivityCompat.requestPermissions(settings.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (!cameraAccepted) {
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
            if (extras != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                if (imageBitmap != null) {
                    ImageView imageView = new ImageView(settings.this); // Create a new ImageView for each photo
                    imageView.setImageBitmap(imageBitmap);
                    imageContainer.addView(imageView); // Add to the container

                    String imagePath = saveBitmapToFile(imageBitmap);
                    if (imagePath != null) {
                        imagePaths.add(imagePath);
                    } else {
                        Toast.makeText(settings.this, "Failed to save the image.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(settings.this, "No image data found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(settings.this, "No extras found.", Toast.LENGTH_SHORT).show();
            }
            // Uncomment the following line if you want to keep taking pictures
             dispatchTakePictureIntent();
        }
    }



    private String saveBitmapToFile(Bitmap bitmap) {
        // Validate sessionID before using it.
        if (sessionID == null) {
            // Handle this appropriately. Maybe log an error or show a message to the user.
            return null;
        }

        // Create a timestamp and image filename as before.
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";

        // Use the member variable sessionID to identify the directory.
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), sessionID);

        // Create directory if it doesn't exist.
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        // Save the image.
        File imageFile = new File(storageDir, imageFileName);
        try (FileOutputStream out = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return imageFile.getAbsolutePath();
    }
*/







}
