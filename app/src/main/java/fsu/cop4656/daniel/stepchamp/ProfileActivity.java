package fsu.cop4656.daniel.stepchamp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity" ;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    long weekSteps;
    long monthSteps;
    long yearSteps;
    final CollectionReference users = db.collection("users");
    FirebaseUser user;
    TextView nameBox;
    TextView stepsBox;
    TextView weeklyBox;
    TextView monthlyBox;
    TextView yearlyBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        weekSteps = 0;
        monthSteps= 0;
        yearSteps= 0;
        user = FirebaseAuth.getInstance().getCurrentUser();
        nameBox = findViewById(R.id.nameBox);
        stepsBox = findViewById(R.id.dailyBox);
        weeklyBox = findViewById(R.id.weeklyBox);
        monthlyBox = findViewById(R.id.monthlyBox);
        yearlyBox = findViewById(R.id.yearlyBox);

        nameBox.setText(user.getDisplayName());

        pastDays();
        pastMonth();
        pastYear();

        setSteps();
    }

    public void setSteps(){

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        users.document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User mUser = documentSnapshot.toObject(User.class);
                stepsBox.setText(Long.toString(mUser.totalsteps));

            }
        });



    }

    public void pastDays(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DATE, 1);
        Date today = cal.getTime();
        cal.add(Calendar.DATE, -7);

        Date weekAgo = cal.getTime();
        Timestamp todayStamp = new Timestamp(today);
        Timestamp weekAgoStamp = new Timestamp(weekAgo);
        CollectionReference steps = users.document(user.getUid()).collection("steps");
        Query query = steps
                .whereGreaterThanOrEqualTo("date", weekAgoStamp)
                .whereLessThanOrEqualTo("date", todayStamp);

        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                weekSteps = weekSteps + document.getLong("stepcount");
                            }
                            weeklyBox.setText(Long.toString(weekSteps));

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }
    public void pastMonth(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date today = cal.getTime();
        cal.add(Calendar.DATE, -cal.DAY_OF_MONTH);

        Date weekAgo = cal.getTime();
        Timestamp todayStamp = new Timestamp(today);
        Timestamp weekAgoStamp = new Timestamp(weekAgo);
        CollectionReference steps = users.document(user.getUid()).collection("steps");
        Query query = steps
                .whereGreaterThanOrEqualTo("date", weekAgoStamp)
                .whereLessThanOrEqualTo("date", todayStamp);

        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                monthSteps = monthSteps + document.getLong("stepcount");
                                Log.d(TAG, Long.toString(monthSteps));
                            }
                            monthlyBox.setText(Long.toString(monthSteps));
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


    }

    public void pastYear(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date today = cal.getTime();
        cal.add(Calendar.DATE, -cal.DAY_OF_YEAR);

        Date weekAgo = cal.getTime();
        Timestamp todayStamp = new Timestamp(today);
        Timestamp weekAgoStamp = new Timestamp(weekAgo);
        CollectionReference steps = users.document(user.getUid()).collection("steps");
        Query query = steps
                .whereGreaterThanOrEqualTo("date", weekAgoStamp)
                .whereLessThanOrEqualTo("date", todayStamp);

        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                yearSteps = yearSteps + document.getLong("stepcount");
                            }
                            yearlyBox.setText(Long.toString(yearSteps));

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }





}
