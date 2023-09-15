package com.example.planteye;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;


public class home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button take_photos = findViewById(R.id.takePhotos);
        //ImageButton photo_set = findViewById(R.id.photoSet);
        //ImageButton settings = findViewById(R.id.settings);
        Button gallery = findViewById(R.id.gallery);

        take_photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settings = new Intent(home.this, settings.class);
                startActivity(settings);
            }
        });



        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settings = new Intent(home.this, GalleryActivity.class);
                startActivity(settings);
            }
        });

    }
}
