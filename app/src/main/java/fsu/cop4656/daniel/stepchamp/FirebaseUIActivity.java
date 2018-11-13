package fsu.cop4656.daniel.stepchamp;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.support.design.widget.Snackbar;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseUIActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    private static final String TAG = "FirebaseUIActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_ui);
        authenticate(); // start sign-in process
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

    public void createSignInIntent(){
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
                Context c = getApplicationContext();
                saveToDatabase(user,c);
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


    public static boolean saveToDatabase(FirebaseUser acct,Context c) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");

        final LocationManager lm = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);

        Location lastLoc = lm.getLastKnownLocation( LocationManager.NETWORK_PROVIDER);
        double latitude = 0;
        double longitude = 0;
       if(lastLoc != null) {
            latitude = lastLoc.getLatitude();
           longitude = lastLoc.getLongitude();
       }
        User user = new User(acct.getDisplayName(),Double.toString(latitude),Double.toString(longitude),0);

        String key = myRef.push().getKey();
        Map<String, Object> postValues = user.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, postValues);
        myRef.updateChildren(childUpdates);
        return true;
    }





}
