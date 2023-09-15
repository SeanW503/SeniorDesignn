package com.example.planteye;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planteye.ImageAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        File rootFolder = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File[] sessionFolders = rootFolder.listFiles();

        ImageAdapter imageAdapter = new ImageAdapter(sessionFolders);
        recyclerView.setAdapter(imageAdapter);

        imageAdapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(File folder) {
                Intent intent = new Intent(GalleryActivity.this, ImageListActivity.class);
                intent.putExtra("folderPath", folder.getAbsolutePath());
                startActivity(intent);
            }
        });
    }
}


