package com.example.planteye;

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
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class settings extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 200;
    private CameraDevice cameraDevice;
    private TextureView textureView;

    private Button captureButton;
    private ImageReader imageReader;
    private List<byte[]> capturedImages = new ArrayList<>();

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

        captureButton = findViewById(R.id.captureButton);
        textureView = findViewById(R.id.textureView);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capturePhoto();
            }
        });

        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                startPreview(surface);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                // Handle the change of the size of the surface
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                // A new frame is available; you might want to process the frame
            }
        });

        Button sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });
    }

    private void openCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    Log.d("Camera", "Camera opened");
                    cameraDevice = camera;

                    if (textureView.isAvailable()) {
                        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
                        startPreview(surfaceTexture);
                    }
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    Log.d("Camera", "Camera disconnected");
                    cameraDevice.close();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.d("Camera", "Camera error: " + error);
                    cameraDevice.close();
                    cameraDevice = null;
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startPreview(SurfaceTexture surfaceTexture) {
        if (cameraDevice == null || surfaceTexture == null) {
            Log.e("startPreview", "startPreview failed.");
            return;
        }

        try {
            surfaceTexture.setDefaultBufferSize(textureView.getWidth(), textureView.getHeight());
            Surface surface = new Surface(surfaceTexture);
            final CaptureRequest.Builder requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            requestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.setRepeatingRequest(requestBuilder.build(), null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    // Handle failure
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void showCapturedImages() {
        // Start the GalleryActivity and pass the list of captured image data
        Intent galleryIntent = new Intent(this, GalleryActivity.class);
        galleryIntent.putExtra("capturedImages", capturedImages.toArray(new byte[0][]));
        startActivity(galleryIntent);
    }

    private void saveCapturedImage(Image image) {
        // ... (existing code)
        if (image == null) {
            Log.e("saveCapturedImage", "Image is null.");
            return;
        }
        // Convert the Image data to a byte array and add it to the list
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        capturedImages.add(bytes);

        // Start the GalleryActivity and pass the captured image data
        // Start the GalleryActivity and pass the captured image data
// Create an intent to start the gallery activity
        Intent galleryIntent = new Intent(this, GalleryActivity.class);

// Convert the List<byte[]> to a Serializable ArrayList for passing
        ArrayList<byte[]> capturedImagesList = new ArrayList<>(capturedImages);
        galleryIntent.putExtra("capturedImages", capturedImagesList);

        startActivity(galleryIntent);


    }


    private void capturePhoto() {
        if (cameraDevice == null) {
            return;
        }

        try {
            imageReader = ImageReader.newInstance(
                    textureView.getWidth(),
                    textureView.getHeight(),
                    ImageFormat.JPEG,
                    1
            );

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());

            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

            cameraDevice.createCaptureSession(Collections.singletonList(imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                            @Override
                            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                super.onCaptureCompleted(session, request, result);
                                Image image = imageReader.acquireNextImage();
                                saveCapturedImage(image);
                            }
                        }, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    // Handle configuration failure
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private int getOrientation(int rotation) {
        int sensorOrientation = 0; // Set this to the actual sensor orientation of your device

        // Map the device's rotation to the JPEG orientation
        switch (rotation) {
            case Surface.ROTATION_0:
                return sensorOrientation;
            case Surface.ROTATION_90:
                return (sensorOrientation + 90) % 360;
            case Surface.ROTATION_180:
                return (sensorOrientation + 180) % 360;
            case Surface.ROTATION_270:
                return (sensorOrientation + 270) % 360;
            default:
                return sensorOrientation;
        }
    }




}
