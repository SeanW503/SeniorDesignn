package com.example.planteye;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class settings extends AppCompatActivity {
        private static final int CAMERA_REQUEST_CODE = 200;
        private PreviewView previewView;
        private Button captureButton;
        private ImageView capturedImageView;  // Added ImageView for displaying the captured image
        private ImageCapture imageCapture;
        private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
        private Executor executor = Executors.newSingleThreadExecutor(); // Added executor


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_settings);

            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                return;
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            }

            previewView = findViewById(R.id.previewView);
            captureButton = findViewById(R.id.captureButton);
            capturedImageView = findViewById(R.id.capturedImageView); // Initialize the ImageView

            captureButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    capturePhoto();
                }
            });

            cameraProviderFuture = ProcessCameraProvider.getInstance(this);
            cameraProviderFuture.addListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                        bindCameraUseCases(cameraProvider);
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, ContextCompat.getMainExecutor(this));
        }

        private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
            Preview preview = new Preview.Builder().build();
            CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build();

            imageCapture = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build();

            preview.setSurfaceProvider(previewView.getSurfaceProvider());

            Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);
        }

        private void capturePhoto() {
            if (imageCapture == null) {
                return;
            }

            File photoFile = createPhotoFile();
            ImageCapture.OutputFileOptions outputFileOptions =
                    new ImageCapture.OutputFileOptions.Builder(photoFile).build();

            imageCapture.takePicture(
                    outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            // Image saved, you can add your logic here
                            // Load and display the captured image in the ImageView
                            Bitmap capturedBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                            capturedImageView.setImageBitmap(capturedBitmap);
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            exception.printStackTrace();
                        }
                    });
        }

        private File createPhotoFile() {
            // Create a file to save the captured image
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            try {
                return File.createTempFile(imageFileName, ".jpg", storageDir);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }



