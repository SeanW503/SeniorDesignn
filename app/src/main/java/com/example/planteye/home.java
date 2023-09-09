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
        ImageButton photo_set = findViewById(R.id.photoSet);
        ImageButton settings = findViewById(R.id.settings);

        take_photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Code to start the takePhotos activity
                Intent intent = new Intent(home.this, takePhotos.class);
                startActivity(intent);
            }
        });

        photo_set.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent photo_set = new Intent(home.this, photoSet.class);
                startActivity(photo_set);
            }
        });


        settings.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent settings = new Intent(home.this, settings.class);
                startActivity(settings);
            }
        });
    }
}
