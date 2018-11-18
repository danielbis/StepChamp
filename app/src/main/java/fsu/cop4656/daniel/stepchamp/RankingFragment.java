package fsu.cop4656.daniel.stepchamp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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


public class RankingFragment extends Fragment {

    ListView lv;
    public RankingFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking, container, false);

        lv = (ListView) view.findViewById(R.id.rankinglist);
        updateTest();
        loadTopUsers();

        return view;
    }

    public void loadTopUsers(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");

       final String[] namearray = new String[3];
        final String[] steparray = new String[3];


        Query query = FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("totalsteps")
                .limitToLast(3);
        // https://stackoverflow.com/questions/30933328/how-to-convert-firebase-data-to-java-object


        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 2;
                for(DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    namearray[i] = (String) messageSnapshot.child("nickname").getValue();
                    long value = (long) messageSnapshot.child("totalsteps").getValue();
                    steparray[i] = Long.toString(value);
                  i--;
                }

                List<Map<String,String>> data = new ArrayList<Map<String,String>>();
                HashMap<String, String> map;

                for (i = 1; i < namearray.length; i++) {
                    map = new HashMap<String, String>();
                    map.put("name", namearray[i]);
                    map.put("address", steparray[i]);
                    data.add(map);
                }
                SimpleAdapter adapter = new SimpleAdapter( getActivity(), data, android.R.layout. simple_list_item_2,
                        new String[] { "name", "address" }, new int[] { android.R.id. text1,
                        android.R.id. text2 });
                lv.setAdapter(adapter);
                // Inflate the layout for this fragment

            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("FAIL", "Failed to read value.", error.toException());
            }
        });

    }

    public void updateTest(){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("users/" + user.getUid());
        DatabaseReference mSteps = myRef.child("totalsteps");
        mSteps.setValue(250);

    }

}
