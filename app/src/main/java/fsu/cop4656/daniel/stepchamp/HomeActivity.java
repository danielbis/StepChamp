package fsu.cop4656.daniel.stepchamp;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Service.START_STICKY;
import static java.lang.Math.toIntExact;


public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private SensorManager mSensorManager;
    private Sensor mStepCounterSensor;

    private GoogleMap mMap;
    Marker marker0;
    Marker marker1;
    Marker marker2;
    Marker marker3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

       /* mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorManager.registerListener(this, mStepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);*/

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        // Add a marker in Sydney, Australia, and move the camera.
        LatLng FSU = new LatLng(30.441210, -84.298050);


        Query query = FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("totalsteps")
                .limitToLast(3);
        // https://stackoverflow.com/questions/30933328/how-to-convert-firebase-data-to-java-object
        final Double[] latitudearray = new Double[4];
        final Double[] longitudearray = new Double[4];
        final int[] steparray = new int[4];



        final String[] namearray = new String[4];
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                for(DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {

                    String latitude = (String) messageSnapshot.child("latitude").getValue();
                    Double latval = Double.parseDouble(latitude);
                    latitudearray[i] = latval;

                    String longitude = (String) messageSnapshot.child("longitude").getValue();
                    Double longval = Double.parseDouble(longitude);
                    longitudearray[i] = longval;
                    namearray[i] = (String) messageSnapshot.child("nickname").getValue();

                    int steps = (int)(long) messageSnapshot.child("totalsteps").getValue();
                    int stepval = steps;
                    steparray[i] = stepval;

                    i++;

                    if( i == 3) {

                    break;
                    }
                }
                Log.d("LAT", "value = " + i );
                for(int j = i-1; j >-1; j--) {

                    Log.i("NAME",namearray[j]);
                    Log.d("LAT", "value = " + latitudearray[j]);
                    Log.d("LONG", "value = " + longitudearray[j]);
                    if(j==0){
                        marker0 = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitudearray[j], longitudearray[j]))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            .title(namearray[j])
                            .snippet( "Steps: " + steparray[j]));
                   }
                    if(j==1){
                        marker1 = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitudearray[j], longitudearray[j]))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                                .title(namearray[j])
                                .snippet( "Steps: " + steparray[j]));
                    }
                    if(j==2){
                       marker2 = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitudearray[j], longitudearray[j]))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                .title(namearray[j])
                                .snippet( "Steps: " + steparray[j]));
                    }
                    if(j==3){
                        marker3 = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitudearray[j], longitudearray[j]))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                                .title(namearray[j])
                                .snippet( "Steps: " + steparray[j]));
                    }

                }

            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("FAIL", "Failed to read value.", error.toException());
            }
        });






        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(FSU,(float)15.25));
    }

    public void SelectMarker(int position){
        Log.i("POSITION:","POS:" + position);

        if(position == 0){
            marker2.showInfoWindow();
        }
        if(position == 1){
            marker1.showInfoWindow();
        }
        if(position == 2){
            marker0.showInfoWindow();
        }

        return;
    }




    public void onSensorChanged(SensorEvent event){
        final float[] values = event.values;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("users/" + user.getUid());
        Query query = FirebaseDatabase.getInstance().getReference("users/"+ user.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DatabaseReference mSteps = myRef.child("totalsteps");
                User mUser = dataSnapshot.getValue(User.class);

                long localSteps = mUser.totalsteps;


                mSteps.setValue(localSteps + (long)values[0]);
                // Inflate the layout for this fragment

            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("FAIL", "Failed to read value.", error.toException());
            }
        });

        Log.i("RANKING", "userdID: " + String.valueOf(user.getUid()));
        Log.d("onSensorChanged", "value: " + String.valueOf(values[0]));
    }



    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("onSensorAccuracyChanged", "value: " + String.valueOf(accuracy));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.signOutItem:
                signOut();
                break;


        }

        return true;
    }

    public void signOut() {
        // [START auth_fui_signout]
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(new Intent(HomeActivity.this, MainActivity.class));
                        finish();
                    }
                });
        // [END auth_fui_signout]
    }
}
