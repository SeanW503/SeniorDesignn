package com.example.planteye;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.exception.MsalClientException;
import com.microsoft.identity.client.exception.MsalException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class settings extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 200;
    private PreviewView previewView;
    private PublicClientApplication msalApp;
    private AuthenticationCallback authenticationCallback;


    private Button captureButton;
    private Button finishCaptureButton;
    private List<byte[]> capturedImages = new ArrayList<>();
    private ImageCapture imageCapture;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 1. Initialize MSAL
        new InitializeMsalAppTask().execute();

        // 2. Set up the UI elements
        previewView = findViewById(R.id.previewView);
        finishCaptureButton = findViewById(R.id.finishCaptureButton);
        captureButton = findViewById(R.id.captureButton);
        finishCaptureButton.setEnabled(false);

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






    private class InitializeMsalAppTask extends AsyncTask<Void, Void, PublicClientApplication> {

        private Exception backgroundException = null;


        @Override
        protected PublicClientApplication doInBackground(Void... voids) {
            try {
                return (PublicClientApplication) PublicClientApplication.createMultipleAccountPublicClientApplication(
                        getApplicationContext(),
                        R.raw.msal_config
                );
            } catch (InterruptedException | MsalException e) {
                backgroundException = e;
                e.printStackTrace();
                return null;
            }
        }


        @Override
        protected void onPostExecute(PublicClientApplication result) {
            if (result == null && backgroundException != null) {
                Log.e("MSAL", "Error initializing msalApp", backgroundException);
                // Inform the user, if necessary
            } else {
                msalApp = result;
                captureButton.setEnabled(true);
                finishCaptureButton.setEnabled(true);
            }
        }

    }






    private void uploadToOneDrive(String accessToken) {
        for (byte[] imageBytes : capturedImages) {
            new UploadToOneDriveTask(accessToken, imageBytes).execute();
        }
    }

    private class UploadToOneDriveTask extends AsyncTask<Void, Void, Boolean> {
        private String accessToken;
        private byte[] imageBytes;
        private static final String UPLOAD_URL = "https://graph.microsoft.com/v1.0/me/drive/root:/PlantEyeFolder/"; // Modify folder path as needed

        public UploadToOneDriveTask(String accessToken, byte[] imageBytes) {
            this.accessToken = accessToken;
            this.imageBytes = imageBytes;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL(UPLOAD_URL + System.currentTimeMillis() + ".jpg:/content"); // Using current time for unique filenames
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setRequestProperty("Content-Type", "image/jpeg");
                conn.setDoOutput(true);
                conn.getOutputStream().write(imageBytes);

                int responseCode = conn.getResponseCode();
                return responseCode == 200 || responseCode == 201; // 200 OK or 201 Created indicates success
            } catch (Exception e) {
                Log.e("PlantEye", "Upload error", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Log.d("PlantEye", "Uploaded successfully!");
            } else {
                Log.e("PlantEye", "Failed to upload.");
            }
        }
    }


}
