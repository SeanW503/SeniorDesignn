package com.example.planteye;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.params.StreamConfigurationMap;
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
import java.util.Collections;
//end of imports for camera2 api

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
    public static final String EXTRA_IMAGE_PATH = "com.example.planteye.IMAGE_PATH";
    private static final int REQUEST_CODE = 1;
    private static final int IMAGE_WIDTH = 640;
    private static final int IMAGE_HEIGHT = 320;
    private String currentPhotoPath;

    private EditText editText;
    private Button sendButton;
    private Button finishCaptureButton;  // <-- Declare the variable here
    private String sessionID;

    private RequestQueue requestQueue;

    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession cameraCaptureSessions;
    private Size imageDimension;
    private ImageReader imageReader;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;


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

        textureView = findViewById(R.id.textureView);

        //Check and request camera permission
        //if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        //} else {
           //openCamera();
        //}



        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sessionID == null) {
                    sessionID = "Session_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                }
                String inputValue = editText.getText().toString();
                int intValue = Integer.parseInt(inputValue);
                sendHttpRequest("http://192.168.4.1/", intValue);
                capturePicture();
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

    private void updatePreview() {
        if (cameraDevice == null) {
            return;
        }

        try {
            // Set up the capture request for the camera preview
            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);

            // Create a camera capture session callback
            CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    // You can add custom logic here if needed
                }
            };

            // Start capturing the camera preview
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), captureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Collections.singletonList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            if (cameraDevice == null) {
                                return;
                            }
                            cameraCaptureSessions = cameraCaptureSession;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            Toast.makeText(settings.this, "Configuration change", Toast.LENGTH_SHORT).show();
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };
    private void openCamera(){
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0]; // Use the first camera
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            // Choose an appropriate size for the preview. You can adjust IMAGE_WIDTH and IMAGE_HEIGHT as needed.
            Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
            for (Size size : sizes) {
                if (size.getWidth() <= IMAGE_WIDTH && size.getHeight() <= IMAGE_HEIGHT) {
                    imageDimension = size;
                    break;
                }
            }

            // Check if the app has permission to access the camera
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
                return;
            }

            // Initialize and configure the ImageReader
            imageReader = ImageReader.newInstance(imageDimension.getWidth(), imageDimension.getHeight(), ImageFormat.JPEG, 1);
            imageReader.setOnImageAvailableListener(imageAvailableListener, mBackgroundHandler);

            // Open the camera
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            // This method is called when a new image is available
            Image image = reader.acquireLatestImage();
            // You can process the captured image here or save it to a file
            // Don't forget to close the image to release resources
            image.close();
        }
    };

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                // Handle permission denied
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread(); // Start the background thread
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    protected void onPause() {
        closeCamera();
        stopBackgroundThread(); // Stop the background thread
        super.onPause();
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            // The TextureView is available; you can open the camera here if needed
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            // Handle size changes, if necessary
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            // The TextureView is being destroyed; release the camera resources
            closeCamera();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            // Called when the TextureView's buffer is updated
        }
    };

    private void closeCamera() {
        if (cameraCaptureSessions != null) {
            cameraCaptureSessions.close();
            cameraCaptureSessions = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }

    private void capturePicture() {
        if (cameraDevice == null) {
            return;
        }

        try {
            // Create a CaptureRequest.Builder for still capture
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureRequestBuilder.addTarget(imageReader.getSurface()); // Set the target surface for the image

            // Capture a still image
            cameraCaptureSessions.capture(captureRequestBuilder.build(), captureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            // The image capture is complete; you can save or process the image here
        }
    };

}



//After this point is the camera/picture taking code as of 9/18/2023 we are trying to implement Camera2 api to replace the old depreciated camera api





   /* // Method to start the camera activity
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










