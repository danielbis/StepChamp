package fsu.cop4656.daniel.stepchamp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    TextView nameBox;
    TextView stepsBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameBox = findViewById(R.id.nameBox);
        stepsBox = findViewById(R.id.stepsBox);

        nameBox.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        setSteps();
    }

    public void setSteps(){

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Query query = FirebaseDatabase.getInstance().getReference("users/"+ user.getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User mUser = dataSnapshot.getValue(User.class);

                stepsBox.setText(Long.toString(mUser.totalsteps));

            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("FAIL", "Failed to read value.", error.toException());
            }
        });


    }


}
