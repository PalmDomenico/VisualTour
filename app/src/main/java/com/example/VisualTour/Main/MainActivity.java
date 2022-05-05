package com.example.VisualTour.Main;

 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Build;
 import android.os.Bundle;

 import com.example.VisualTour.POI.privatePOI;
 import com.example.VisualTour.POI.publicPOI;
 import com.example.VisualTour.R;
import com.google.android.material.navigation.NavigationView;

 import androidx.appcompat.app.ActionBar;
 import androidx.appcompat.app.AppCompatActivity;

import android.os.StrictMode;
 import android.view.MenuItem;

 import androidx.core.content.ContextCompat;
 import androidx.drawerlayout.widget.DrawerLayout;
 import androidx.fragment.app.Fragment;
 import androidx.navigation.NavController;
 import androidx.navigation.NavHostController;
 import androidx.navigation.Navigation;
 import androidx.navigation.fragment.NavHostFragment;
 import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.VisualTour.databinding.ActivityMainBinding;
 import com.mapbox.api.directions.v5.models.DirectionsRoute;

public class MainActivity extends AppCompatActivity {
     public static AppBarConfiguration mAppBarConfiguration;
    public static NavController navController;
    public static NavigationView navigationView;
    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        getSupportActionBar().setDisplayOptions(ActionBar. DISPLAY_SHOW_CUSTOM);
        Source: https://frameboxxindore.com/android/how-can-change-action-bar-title-in-android-programmatically.html
        getSupportActionBar(). setDisplayShowCustomEnabled(true);

        getSupportActionBar().setTitle("wffffffffffff");
        getSupportActionBar().show();





        DrawerLayout drawer = binding.drawerLayout;
        navigationView = binding.navView;
        mAppBarConfiguration = new AppBarConfiguration.Builder(
               R.id.homeMappa,R.id.publicMap,R.id.publicPOI,R.id.account,R.id.private_map,R.id.privatePOI,R.id.login)
               .setOpenableLayout(drawer)
                .build();
          navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
         NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

         NavigationUI.setupWithNavController(navigationView, navController);//attiva i bottoni nella barra
         StrictMode.ThreadPolicy gfgPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(gfgPolicy);

        if (!allPermissionsGranted() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(getRequiredPermissions(), PERMISSIONS_REQUEST_CODE);
        }
    }
    private static final int PERMISSIONS_REQUEST_CODE = 123;
    private static final String PERMISSION_FOREGROUND_SERVICE = "android.permission.FOREGROUND_SERVICE";

    protected boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // PERMISSION_FOREGROUND_SERVICE was added for targetSdkVersion >= 28, it is normal and always granted, but should be added to the Manifest file
                // on devices with Android < P(9) checkSelfPermission(PERMISSION_FOREGROUND_SERVICE) can return PERMISSION_DENIED, but in fact it is GRANTED, so skip it
                // https://developer.android.com/guide/components/services#Foreground
                if (permission.equals(PERMISSION_FOREGROUND_SERVICE)) {
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    private String[] getRequiredPermissions() {
        String[] permissions;
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] requestedPermissions = info.requestedPermissions;
            if (requestedPermissions != null && requestedPermissions.length > 0) {
                permissions = requestedPermissions;
            } else {
                permissions = new String[]{};
            }
        } catch (PackageManager.NameNotFoundException e) {
            permissions = new String[]{};
        }

        return permissions;
    }



    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }



}