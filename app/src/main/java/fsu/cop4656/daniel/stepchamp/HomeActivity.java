package fsu.cop4656.daniel.stepchamp;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Service.START_STICKY;
import static java.lang.Math.toIntExact;


public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "HomeActivity";
    private SensorManager mSensorManager;
    private Sensor mStepCounterSensor;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    final CollectionReference users = db.collection("users");
    long dailySteps;
    private GoogleMap mMap;
    Marker marker0;
    Marker marker1;
    Marker marker2;
    Marker marker3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getOrCreateDailySteps();
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


        com.google.firebase.firestore.Query query = users.orderBy("totalsteps").limit(3);
        // https://stackoverflow.com/questions/30933328/how-to-convert-firebase-data-to-java-object
        final Double[] latitudearray = new Double[4];
        final Double[] longitudearray = new Double[4];
        final int[] steparray = new int[4];



        final String[] namearray = new String[4];
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int i = 0;
                for(QueryDocumentSnapshot messageSnapshot :queryDocumentSnapshots) {
                    User tempUser = messageSnapshot.toObject(User.class);
                    String latitude = tempUser.latitude;
                    Double latval = Double.parseDouble(latitude);
                    latitudearray[i] = latval;

                    String longitude = tempUser.longitude;
                    Double longval = Double.parseDouble(longitude);
                    longitudearray[i] = longval;
                    namearray[i] = tempUser.nickname;

                    int steps = (int)(long) tempUser.totalsteps;
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
        final DocumentReference currentUser = users.document(user.getUid());
        Query query = FirebaseDatabase.getInstance().getReference("users/"+ user.getUid());
        Map<String, Object> data = new HashMap<>();
        data.put("totalsteps", values[0]);
        users.document(user.getUid()).set(data, SetOptions.merge());

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
            case R.id.userprofile:
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
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

    public void getOrCreateDailySteps(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        final Timestamp now = new Timestamp(date);
        final CollectionReference currentUserSteps = db.collection("users/" + user.getUid() + "/steps");
        final DocumentReference stepsToday = currentUserSteps.document(now.toString());

        stepsToday.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    dailySteps = documentSnapshot.getLong("stepcount");
                    Log.d(TAG, "Steps for today already created.");
                }else{
                    Map<String, Object> postValues = new HashMap<>();
                    postValues.put("stepcount", 0);
                    postValues.put("date", now);
                    Log.d(TAG, "About to save.");
                    currentUserSteps.document(now.toString()).set(postValues).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Steps added successfully.");
                            dailySteps = 0;
                            Map<String, Object> hLevel = new HashMap<>();
                            hLevel.put("totalsteps", dailySteps);
                            users.document(user.getUid()).set(hLevel, SetOptions.merge());

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Cannot add steps.");

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

    public void simulateWalk(long s){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        final Timestamp now = new Timestamp(date);
        final CollectionReference currentUserSteps = db.collection("users/" + user.getUid() + "/steps");
        final DocumentReference stepsToday = currentUserSteps.document(now.toString());
        Map<String, Object> postValues = new HashMap<>();
        postValues.put("stepcount",  dailySteps + s);
        postValues.put("date", now);
        stepsToday.set(postValues);
        Map<String, Object> hLevel = new HashMap<>();
        hLevel.put("totalsteps", dailySteps +s);
        users.document(user.getUid()).set(hLevel, SetOptions.merge());
    }


}
