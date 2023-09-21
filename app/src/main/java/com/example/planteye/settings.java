package com.example.planteye;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;

import androidx.camera.view.PreviewView;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LifecycleOwner;
import com.google.common.util.concurrent.ListenableFuture;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

// ... [other imports]

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class settings extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 200;
    private PreviewView previewView;
    private Button captureButton;
    private Button finishCaptureButton; // New button for triggering the upload to OneDrive
    private List<byte[]> capturedImages = new ArrayList<>();
    private ImageCapture imageCapture;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        previewView = findViewById(R.id.previewView);
        finishCaptureButton = findViewById(R.id.finishCaptureButton);

        finishCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadToOneDrive();
            }
        });
        captureButton = findViewById(R.id.captureButton);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capturePhoto();
            }
        });

        // Check permissions and then start the camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            startCamera(); // Initialize the camera if permission is granted.
        }
    }



    // ... [rest of the code]



    private void capturePhoto() {
        if (imageCapture == null) {
            Log.d("CameraDebug", "imageCapture is null. Cannot capture image.");
            return;
        }

        Log.d("CameraDebug", "Attempting to capture image...");

        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                Log.d("CameraDebug", "Image capture successful.");

                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                capturedImages.add(bytes);

                Log.d("CameraDebug", "Image stored in array.");

                image.close();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("CameraDebug", "Image capture failed: " + exception.getMessage());
                exception.printStackTrace();
            }
        });
    }


    private void uploadToOneDrive() {
        // Ensure you have an access token (using MSAL or any other way you've set up authentication)
        String accessToken = ...;  // obtain this using your authentication method

        // Check if the "PlantEye" folder exists or create one
        String folderId = ensureFolderExists(accessToken, "PlantEye");

        for (byte[] imageBytes : capturedImages) {
            uploadImage(accessToken, folderId, imageBytes);
        }

        // Clear the list after uploading
        capturedImages.clear();
    }

    private String ensureFolderExists(String accessToken, String folderName) {
        // Here, use the Graph API to check if the folder exists
        // If it doesn't, create it
        // Return the folder ID either way

    }

    private void uploadImage(String accessToken, String folderId, byte[] imageBytes) {
        // Use the Graph API to upload the imageBytes to the specified folder using the provided access token

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
        ImageCapture.Builder builder = new ImageCapture.Builder();
        imageCapture = builder.build();

        cameraProvider.unbindAll();
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);

        preview.setSurfaceProvider(previewView.getSurfaceProvider());
    }

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

}
