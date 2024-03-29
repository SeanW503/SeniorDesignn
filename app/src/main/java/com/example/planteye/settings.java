package com.example.planteye;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.exception.MsalException;

import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class settings extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 200;
    private PreviewView previewView;
    private PublicClientApplication msalApp;
    private AuthenticationCallback authenticationCallback;


    private Button captureButton;
    private Button finishCaptureButton;
    private Button sendButton;
    private List<byte[]> capturedImages = new ArrayList<>();
    private ImageCapture imageCapture;
    private EditText editText_two;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private RequestQueue requestQueue;


    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());

    private EditText editTextPlantName;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 1. Initialize MSAL
        initializeMsalApp();

        // 2. Set up the UI elements
        previewView = findViewById(R.id.previewView);
        finishCaptureButton = findViewById(R.id.finishCaptureButton);
        captureButton = findViewById(R.id.captureButton);
        sendButton = findViewById(R.id.sendButton);
        finishCaptureButton.setEnabled(false);
        editText_two = findViewById(R.id.editText);
        editTextPlantName = findViewById(R.id.plantNameEditText);


        requestQueue = Volley.newRequestQueue(this);

        // 3. Set the click listeners for the buttons
        finishCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadToOneDrive();
            }
        });

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capturePhoto();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendButton.setEnabled(false);

                String inputValue = editText_two.getText().toString();
                int totalDegrees;
                try {
                    totalDegrees = Integer.parseInt(inputValue);
                } catch (NumberFormatException e) {
                    Toast.makeText(settings.this, "Invalid degree input", Toast.LENGTH_SHORT).show();
                    sendButton.setEnabled(true);
                    return;
                }

                int numberOfPhotos = 360 / totalDegrees;
                takePhotosInSequence(totalDegrees, numberOfPhotos);
            }
        });



        // 4. Initialize the authentication callback
        authenticationCallback = new AuthenticationCallback() {
            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                String accessToken = authenticationResult.getAccessToken();
                uploadToOneDrive(accessToken); // Pass the token to your upload function.
            }

            @Override
            public void onError(MsalException exception) {
                Log.e("MSAL", "Authentication failed: " + exception.toString());
            }

            @Override
            public void onCancel() {
                Log.d("MSAL", "User cancelled authentication.");
            }
        };

        // 5. Check permissions and start the camera if necessary
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            startCamera(); // Initialize the camera if permission is granted.
        }
    }


    private int photoCounter = 0;

    private void takePhotosInSequence(int totalDegrees, int numberOfPhotos) {
        if (photoCounter < numberOfPhotos) {
            sendHttpRequest("http://192.168.4.1/", totalDegrees);

            // Delay for 5 seconds
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    capturePhoto();

                    // Delay for another 5 seconds before the next rotation and photo
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            photoCounter++;
                            takePhotosInSequence(totalDegrees, numberOfPhotos);
                        }
                    }, 5000);
                }
            }, 5000);
        } else {
            photoCounter = 0; // Reset counter
            sendButton.setEnabled(true);
        }
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


    private void signIn() {
        if (msalApp == null) {
            Log.e("MSAL", "msalApp is not initialized.");
            return;
        }
        msalApp.acquireToken(this, new String[]{"Files.ReadWrite"}, authenticationCallback);
    }


    private void capturePhoto() {
        if (imageCapture == null) {
            Log.d("CameraDebug", "imageCapture is null. Cannot capture image.");
            return;
        }

        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                capturedImages.add(bytes);
                image.close();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("CameraDebug", "Image capture failed: " + exception.getMessage());
            }
        });
    }

    private void uploadToOneDrive() {
        signIn();  // Request the token before uploading.
    }



    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("PlantEye", "Error starting the camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Preview preview = new Preview.Builder().build();
        imageCapture = new ImageCapture.Builder().build();

        cameraProvider.unbindAll();
        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

        preview.setSurfaceProvider(previewView.getSurfaceProvider());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Log.e("PlantEye", "Camera permission denied.");
            }
        }
    }






    private void initializeMsalApp() {
        executorService.execute(() -> {
            try {
                final PublicClientApplication result = (PublicClientApplication) PublicClientApplication.createMultipleAccountPublicClientApplication(
                        getApplicationContext(),
                        R.raw.msal_config
                );
                handler.post(() -> {
                    msalApp = result;
                    captureButton.setEnabled(true);
                    finishCaptureButton.setEnabled(true);
                });
            } catch (InterruptedException | MsalException e) {
                handler.post(() -> {
                    Log.e("MSAL", "Error initializing msalApp", e);
                    // Inform the user, if necessary
                });
            }
        });
    }




    private void uploadToOneDrive(String accessToken, List<byte[]> capturedImages) {
        executorService.execute(() -> {
            boolean result = performUpload(accessToken, capturedImages);
            handler.post(() -> {
                if (result) {
                    logDebug("Uploaded successfully!");
                    Toast.makeText(settings.this, "Uploaded Successfully!", Toast.LENGTH_LONG).show();
                } else {
                    logError("Failed to upload.");
                    Toast.makeText(settings.this, "Upload Failed!", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private int imageCounter = 0;

    private boolean performUpload(String accessToken, List<byte[]> capturedImages) {
        try {
            final String UPLOAD_URL = "https://graph.microsoft.com/v1.0/me/drive/root:/PlantEye/";

            // Retrieve the plant name and degree value from the EditText fields
            String plantNameValue = editTextPlantName.getText().toString().trim();
            String degreeValue = editText_two.getText().toString().trim();

            // Check if the values are empty
            if (plantNameValue.isEmpty() || degreeValue.isEmpty()) {
                Toast.makeText(settings.this, "Please provide both Plant Name and Degree", Toast.LENGTH_SHORT).show();
                return false;
            }

            // Convert degreeValue to an integer
            int degree = Integer.parseInt(degreeValue);
            int numberOfPhotos = 360 / degree;

            // Ensure capturedImages has enough images
            if (capturedImages.size() != numberOfPhotos) {
                Toast.makeText(settings.this, "Number of captured images doesn't match the required amount for the degree.", Toast.LENGTH_SHORT).show();
                return false;
            }

            // Construct the image path for OneDrive for each photo
            for (int i = 0; i < numberOfPhotos; i++) {
                // Calculate the current degree based on the loop index
                int currentDegree = degree * i;

                // Calculate the folder path for the current degree value
                String folderPath;
                if (currentDegree == 360) {
                    folderPath = plantNameValue + "/sv_" + degreeValue + "/";
                } else {
                    folderPath = plantNameValue + "/sv_" + currentDegree + "/";
                }

                // Construct the image path for the current photo and did some cool things

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
                String imagePath = folderPath + timeStamp + ".jpg";

                URL url = new URL(UPLOAD_URL + imagePath + ":/content");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setRequestProperty("Content-Type", "image/jpeg");
                conn.setDoOutput(true);

                // Use the byte array of the current image for upload
                byte[] imageBytes = capturedImages.get(i);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(imageBytes);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode != 200 && responseCode != 201) {
                    String errorResponse = getErrorResponseBody(conn);
                    logError("Upload failed with response code: " + responseCode);
                    logError("Error response: " + errorResponse);
                    return false;
                }
            }

            logDebug("Uploaded successfully to the respective sv_degree folders.");
            return true;
        } catch (Exception e) {
            logError("Upload error", e);
            return false;
        }
    }


    private String getErrorResponseBody(HttpURLConnection conn) {
        try (Scanner scanner = new Scanner(conn.getErrorStream())) {
            StringBuilder response = new StringBuilder();
            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
            }
            return response.toString();
        } catch (Exception e) {
            return "Error reading error response body.";
        }
    }

    private void logDebug(String message) {
        Log.d("PlantEye", message);
    }

    private void logError(String message) {
        Log.e("PlantEye", message);
    }

    private void logError(String message, Throwable tr) {
        Log.e("PlantEye", message, tr);
    }




    private void uploadToOneDrive(String accessToken) {
        for (byte[] imageBytes : capturedImages) {
            uploadToOneDriveTask(accessToken, capturedImages);

        }
    }

    private void uploadToOneDriveTask(String accessToken, List<byte[]> capturedImages) {
        uploadToOneDrive(accessToken, capturedImages);
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }



}