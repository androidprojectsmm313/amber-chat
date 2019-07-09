package com.app.amber.chat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

public class Location extends AppCompatActivity {
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getApplicationContext(), "pk.eyJ1IjoiYWJkdWxsYWgzMjE0NTYiLCJhIjoiY2p0ZzNubGR3MXo3dDQ5bzlza3Z0Z2V6MiJ9.cnOTzNEfixxD_T7yGMPMiQ");
        setContentView(R.layout.activity_location);
       // Mapbox.getInstance(getApplicationContext(), YOUR_MAPBOX_ACCESS_TOKEN);
        final int PERMISSION_ALL = 1;
        final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};
        if(!hasPermissions(this,PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }


        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {

               mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
                   @Override
                   public boolean onMapClick(@NonNull LatLng point) {
                       System.out.println(point.toString());
                       return false;
                   }
               });

                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {



                        // Map is set up and the style has loaded. Now you can add data or make other map adjustments


                    }
                });
            }
        });
    }





    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("permission granted");
                    //readContacts();read permission granted
                    //sendUserDataToSrver();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        new AlertDialog.Builder(this).
                                setTitle("Access Fine Location").
                                setMessage("Access Fine Location permission is required to use this feature. Retry and grant it !").show();
                    } else {
                        new AlertDialog.Builder(this).
                                setTitle("Access Fine Location").
                                setMessage("You denied read external storage permission." +
                                        " So, the feature will be disabled. To enable it" +
                                        ", go on settings and " +
                                        "grant read phone state permission for the application")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        System.out.println("ok pressed");
                                    }
                                })
                                .setNegativeButton("Canel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(getApplicationContext(), "Application will not work correctly without this permission.", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .show();
                    }
                }
                break;
        }
    }
}
