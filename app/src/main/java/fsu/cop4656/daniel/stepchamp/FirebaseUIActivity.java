package fsu.cop4656.daniel.stepchamp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.support.design.widget.Snackbar;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseUIActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    private static final String TAG = "FirebaseUIActivity";
    private LocationManager mLocationManager;
    private boolean mLocationPermissionGranted = false;
    private double mLongitude = 0;
    private double mLatitude = 0;

    private static final int PERMISSION_REQUEST_LOCATIONS = 3;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_ui);
        mLocationManager = (LocationManager) getSystemService(
                LOCATION_SERVICE);

        authenticate(); // start sign-in process
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationManager.removeUpdates(mLocationListener);
    }

    public void authenticate() {
        // [START check_current_user]
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        } else {
            // No user is signed in
            createSignInIntent();

        }
        // [END check_current_user]
    }

    public void createSignInIntent() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);

    }

    // [START auth_fui_result]
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Intent intent = new Intent(this, HomeActivity.class);
                saveToDatabase(user);
                startActivity(intent);
                // ...
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Log.e(TAG, "Sign-in error: User pressed back button.");
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Log.e(TAG, "Sign-in error: No Internet Connection");

                    return;
                }
                // unknown error
                Log.e(TAG, "Sign-in error: ", response.getError());

            }
        }




    }
    // Location code below
    public void requestLocationUpdates(View v) {
        Log.i(TAG, "Requesting location...");

        if(!mLocationPermissionGranted) {
            requestLocationsPermission();
        }
        try {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    0, 0, mLocationListener);

        } catch (SecurityException e) {
            requestLocationsPermission();
        }
    }

    private void requestLocationsPermission() {

        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)){

            if (ActivityCompat.shouldShowRequestPermissionRationale(FirebaseUIActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(FirebaseUIActivity.this);
                builder.setTitle("Requesting internet permissions");
                builder.setMessage("This application requires internet. Accept to continue");
                builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(FirebaseUIActivity.this,
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSION_REQUEST_LOCATIONS);
                    }
                });
                builder.create();
                builder.show();
            }
            else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(FirebaseUIActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQUEST_LOCATIONS);
            }
        }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_LOCATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
                    Log.i(TAG, "Location permissions were granted.");
                    mLocationPermissionGranted = true;
                }


            } else {
                // Permission request was denied.
                /*Toast.makeText(FirebaseUIActivity.this,
                        "Location permission request was denied. Location permission is required for this application ",
                        Toast.LENGTH_SHORT).show();*/
                Intent intent = new Intent(FirebaseUIActivity.this, MainActivity.class);
                startActivity(intent);
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


    public void saveToDatabase(FirebaseUser acct) {
        final String uid = acct.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final CollectionReference users = db.collection("users");
        DocumentReference currentUser = users.document(uid);

        setLastKnownLocation(); // get the location
        if (mLatitude == 0 && mLongitude == 0){
            mLatitude = 30.441210;
            mLongitude = -84.298050;
        }
        // create user
        final User user = new User(acct.getDisplayName(), Double.toString(mLatitude), Double.toString(mLongitude), 125);


        currentUser.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    Log.d(TAG, "User exists.");
                }else{
                    Map<String, Object> postValues = user.toMap();
                    Log.d(TAG, "About to save.");
                    users.document(uid).set(postValues).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "User created successfully.");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Cannot create user.");

                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failure in get.");

            }
        });

    }
}







