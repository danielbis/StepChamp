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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
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
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RankingFragment extends Fragment {
    long dailySteps;
    Button addSteps;
    static final String TAG = "RankingFragment";

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    final CollectionReference users = db.collection("users");
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

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                ((HomeActivity)getActivity()).SelectMarker(position);
            }
        });

        loadTopUsers();
        addSteps = view.findViewById(R.id.simulateWalk);
        addSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOrCreateDailySteps();
                simulateWalk(100);
                loadTopUsers();

            }
        });


        return view;
    }

    public void loadTopUsers(){


        final String[] namearray = new String[3];
        final String[] steparray = new String[3];


        com.google.firebase.firestore.Query query = users.orderBy("totalsteps").limit(3);
        // https://stackoverflow.com/questions/30933328/how-to-convert-firebase-data-to-java-object


        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int i = 2;
                for(QueryDocumentSnapshot messageSnapshot :queryDocumentSnapshots) {
                    User tempUser = messageSnapshot.toObject(User.class);
                    namearray[i] = tempUser.nickname;
                    long value = (long) tempUser.totalsteps;
                    steparray[i] = Long.toString(value);
                  i--;
                  if(i==-1){
                      break;
                  }
                }

                Log.i("SIZE:","RESULTS:" + i);

                List<Map<String,String>> data = new ArrayList<Map<String,String>>();
                HashMap<String, String> map;

                for (i = 0; i < namearray.length; i++) {
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

        });

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
