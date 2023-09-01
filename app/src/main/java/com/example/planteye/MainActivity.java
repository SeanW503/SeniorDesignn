package com.example.planteye;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.planteye.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
/////////
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
//hi///

    //Runs everytime activity is created(happens once)
    //on start runs everytime you open the screen(IMPORTANT)
    // treat activities as screens on the app

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        /*NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);*/

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();
            }
        });

        TextView tv1 = (TextView)findViewById(R.id.textView);
        tv1.setText("Plant Eye");

/*        tv1.setX(30);
        tv1.setY(30);*/


    }

    public void validateLogin(View v) {
        EditText editTextUsername = findViewById(R.id.usernameEditText);
        EditText editTextPassword = findViewById(R.id.passwordEditText);

        String username = editTextUsername.getText().toString();
        String password = editTextPassword.getText().toString();

        if ("admin".equals(username) && "admin".equals(password)) {
            // User is authenticated; navigate to home activity
            Intent i = new Intent(this, home.class);
            startActivity(i);
        } else {
            // Show a Snackbar message
            Snackbar.make(v, "Invalid Username or Password", Snackbar.LENGTH_LONG)
                    .setAnchorView(R.id.fab)
                    .setAction("Action", null).show();
        }
    }

    public void home(View v){

        validateLogin(v);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    /*@Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }*/
}