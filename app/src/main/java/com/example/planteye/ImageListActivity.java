package com.example.planteye;

import android.os.Bundle;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.planteye.databinding.ActivityImageListBinding;

public class ImageListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_list);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String folderPath = getIntent().getStringExtra("folderPath");
        File folder = new File(folderPath);
        File[] imageFiles = folder.listFiles();

        // Use another RecyclerView adapter to display the images
        // ImageFileAdapter imageFileAdapter = new ImageFileAdapter(imageFiles);
        // recyclerView.setAdapter(imageFileAdapter);
    }
}