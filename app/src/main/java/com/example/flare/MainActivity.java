package com.example.flare;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashSet;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_PHONE_STATE;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
    private LatLng lastKnownLocation;
    private LatLng closestSafety;
    private int phoneNumber;


    HashSet<LatLng> safeLocations = new HashSet<>();


    public final float zoom = 16f;

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
        ActivityCompat.requestPermissions(this, new String[]{READ_PHONE_STATE}, 1);
        ActivityCompat.requestPermissions(this, new String[]{INTERNET}, 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        requestPermission();

        TelephonyManager tMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        String mPhoneNumber = tMgr.getLine1Number();


        LatLng massGen = new LatLng(42.362804, -71.068634);
        LatLng mIT = new LatLng(42.359386, -71.09395);
        LatLng harvard = new LatLng(42.37778, -71.12326);
        safeLocations.add(massGen);
        safeLocations.add(mIT);
        safeLocations.add(harvard);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        Button manual_flare = findViewById(R.id.manual_flare);//get id of manual_button
        manual_flare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if(ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    return;
                }
                fusedLocationClient.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location != null){
                            lastKnownLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            flareConfirmationMessage(view);
                            mMap.addMarker(new MarkerOptions().position(lastKnownLocation).title("Current Location"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation,zoom));
                            LatLng closestSafe = null;
                            for(LatLng loc : safeLocations){
                                if(closestSafe == null){
                                    closestSafe = loc;
                                } else{
                                    if(distFrom(lastKnownLocation.latitude,lastKnownLocation.longitude,loc.latitude,loc.longitude) < distFrom(lastKnownLocation.latitude,lastKnownLocation.longitude,closestSafe.latitude,closestSafe.longitude)){
                                        closestSafe = loc;
                                    }
                                }
                            }
                            mMap.addMarker(new MarkerOptions().position(closestSafe).title("Closest Safety"));
                        } else{
                            flareErrorMessage(view);
                        }
                    }
                });
            }
        });

        Button disaster_guide = findViewById(R.id.disaster_guide);//get id of disaster_guide
        disaster_guide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println(fusedLocationClient);
            }
        });

    }

    public void flareConfirmationMessage(View v){
        Toast.makeText(MainActivity.this, "Flare sent. Help is on the way!", Toast.LENGTH_LONG).show();
    }

    public void flareErrorMessage(View v){
        Toast.makeText(MainActivity.this, "Flare not sent. Find a network or turn on Location Services!", Toast.LENGTH_LONG).show();
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    lastKnownLocation = new LatLng(location.getLatitude(), location.getLongitude());
                }
            }
        });
        if(lastKnownLocation != null){
            mMap.addMarker(new MarkerOptions().position(lastKnownLocation).title("Current Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation,zoom));
        } else{
            return;
        }

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

    public static float distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);
        return dist;
    }

    public interface SmsListener {
        public void messageReceived(String messageText);
    }

}
