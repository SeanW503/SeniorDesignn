package com.example.planteye;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {
    private ArrayList<byte[]> capturedImages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        // Receive the captured image data as a Serializable ArrayList
        capturedImages = (ArrayList<byte[]>) getIntent().getSerializableExtra("capturedImages");

        // Assuming you have a RecyclerView with the ID "recyclerView" in activity_gallery.xml
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create an ImageAdapter and set it to the RecyclerView
        ImageAdapter imageAdapter = new ImageAdapter(capturedImages);
        recyclerView.setAdapter(imageAdapter);
    }
}
