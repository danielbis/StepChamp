package fsu.cop4656.daniel.stepchamp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Button signIn;

    private LocationManager mLocationManager;
    private boolean mLocationPermissionGranted = false;
    private double mLongitude = 0;
    private double mLatitude = 0;

    private static final int PERMISSION_REQUEST_LOCATIONS = 3;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signIn = findViewById(R.id.signInButton);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FirebaseUIActivity.class);
                startActivity(intent);
            }
        });
        mLocationManager = (LocationManager) getSystemService(
                LOCATION_SERVICE);
        checkCurrentUser();
    }

    public void checkCurrentUser() {
        // [START check_current_user]
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            Intent intent = new Intent(this, HomeActivity.class);
            Log.i("TEST","TEST");
            updateLocation(user);
            startActivity(intent);
        } else {
            // No user is signed in
            // Wait for user to click sign in button
            // It will take him to the FirebaseUIActivity

        }
        // [END check_current_user]
    }



    private void requestLocationsPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Requesting internet permissions");
            builder.setMessage("This application requires internet. Accept to continue");
            builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSION_REQUEST_LOCATIONS);
                }
            });
            builder.show();
        } else {
            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_LOCATIONS);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_LOCATIONS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Location permissions were granted.");
            } else {
                // Permission request was denied.
                Toast.makeText(MainActivity.this,
                        "Location permission request was denied. Location permission is required for this application ",
                        Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Location permission request was denied. Requesting again...");
                requestLocationsPermission(); // repeat until they confirm
            }
        }
    }


    private void setLastKnownLocation() {
        if (!mLocationPermissionGranted) {
            requestLocationsPermission();
        }
        Location lastLoc = null;
        try {
            lastLoc = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        } catch (SecurityException e) {
            requestLocationsPermission();
        }
        if(lastLoc == null) {
            Log.e(TAG, "No last known location"); // log to errors
            return;
        }


        mLatitude = lastLoc.getLatitude();
        mLongitude = lastLoc.getLongitude();

        if (mLatitude == 0|| mLongitude == 0) {
            Log.e(TAG, "Location not set!");
        }
    }


    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "Waiting for location",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "Connection Lost",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLocationChanged(Location location) {
            Location lastLoc = null;

            try {
                lastLoc = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            } catch (SecurityException e) {
                requestLocationsPermission();
            }
            mLatitude = lastLoc.getLatitude();
            mLongitude = lastLoc.getLongitude();

            if (mLatitude == 0|| mLongitude == 0) {
                Log.e(TAG, "Location not changed!");
            }
        }
    };


    public void updateLocation(FirebaseUser acct) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("users");
        Log.i("TEST","TEST2");
        Log.i("TEST","TEST3");
        Log.i("TEST","TEST4");
        Log.i("TEST","TEST5");
        Log.i("TEST","TEST6");

        setLastKnownLocation(); // get the location
        final User user = new User(acct.getDisplayName(), Double.toString(mLatitude), Double.toString(mLongitude), 3465);
        final String uid = acct.getUid();
        Query query = FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("nickname").equalTo(acct.getDisplayName());


        query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists()){
                    myRef.child(uid).setValue(user);

                }
                else{

                    for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                        String key = childSnapshot.getKey();
                        Log.i("LAT:", "lat:" + mLatitude);
                        Log.i("Long:", "long:" + mLongitude);

                        if (((mLatitude < 30.447300) && (mLatitude > 30.435546)) && ((mLongitude > -84.306552) && (mLongitude < -84.290877))) {
                            Map<String, Object> postValues = user.toMap();
                            myRef.child(key).child("latitude").setValue(Double.toString(mLatitude));
                            myRef.child(key).child("longitude").setValue(Double.toString(mLongitude));
                            return;
                        }
                    }

                }

            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("FAIL", "Failed to read value.", error.toException());
            }
        });
    }
}





