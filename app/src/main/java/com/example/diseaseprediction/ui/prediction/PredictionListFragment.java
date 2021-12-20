package com.example.diseaseprediction.ui.prediction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diseaseprediction.Login;
import com.example.diseaseprediction.MainActivity;
import com.example.diseaseprediction.R;
import com.example.diseaseprediction.adapter.PredictionAdapter;
import com.example.diseaseprediction.firebase.FirebaseConstants;
import com.example.diseaseprediction.object.Prediction;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class PredictionListFragment extends Fragment {
    private static final String TAG = "PredictionListFragment";

    private FirebaseUser fUser;
    private DatabaseReference mRef;

    private RecyclerView prediction_list_recycler_view_main;
    private List<Prediction> mPredictionList;
    private PredictionAdapter patientPredictionAdapter;
    private TextView prediction_list_txt_title;
    private ShimmerFrameLayout prediction_shimmer;

    private Context context;


    public PredictionListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set toolbar
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.menu_predictionList));
        ((MainActivity) getActivity()).setIconToolbar();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view,
                              @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getActivity().getApplicationContext();

        prediction_list_recycler_view_main.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        //Load UI
        loadUIByType();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        container.removeAllViews();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_prediction_list, container, false);

        //Get current user
        fUser = FirebaseAuth.getInstance().getCurrentUser();

        prediction_list_txt_title = view.findViewById(R.id.prediction_list_txt_title);
        prediction_shimmer = view.findViewById(R.id.prediction_shimmer);
        prediction_shimmer.startShimmer();
        prediction_list_recycler_view_main = view.findViewById(R.id.prediction_list_recycler_view_main);
        prediction_list_recycler_view_main.setHasFixedSize(true);


        return view;
    }

    /**
     * Load UI by account type
     */
    private void loadUIByType() {
        try {
            mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_ACCOUNT).child(fUser.getUid());
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        if (snapshot.child("typeID").getValue().toString().equals("0")) {
                            loadAllPredictionOfAccount(0);
                        } else {
                            loadAllPredictionOfAccount(1);
                        }

                    } catch (NullPointerException e) {
                        Log.e(TAG, "setUIByAccountType: TypeID Null", e);
                        Toast.makeText(context, context.getString(R.string.error_unknown_relogin), Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                        Intent i = new Intent(context, Login.class);
                        startActivity(i);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "loadUIByType()");
        }
    }

    /**
     * Load prediction by type account
     * Load list prediction of patient if account type is 1
     * Load list prediction that confirmed by account doctor if account type is 0
     *
     * @param typeAcc type of account. 0: doctor | 1: patient
     */
    private void loadAllPredictionOfAccount(int typeAcc) {
        try {
            mPredictionList = new ArrayList<>();
            Query predictionByDateUpdate = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_PREDICTION).orderByChild(
                    "dateUpdate/time");
            predictionByDateUpdate.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mPredictionList.clear();
                    for (DataSnapshot sh : snapshot.getChildren()) {
                        Prediction pr = sh.getValue(Prediction.class);
                        //If patientId equal with current accountID
                        try {
                            //Doctor type
                            if (typeAcc == 0) {
                                if (pr.getDoctorID().equals(fUser.getUid())) {
                                    mPredictionList.add(pr);
                                }
                            }
                            //Patient type
                            else if (typeAcc == 1) {
                                if (pr.getPatientID().equals(fUser.getUid())) {
                                    mPredictionList.add(pr);
                                }
                            }
                        } catch (NullPointerException e) {
                            Log.d(TAG, "PredictionListFragment. ID null", e);
                        }
                    }
                    // if list not empty
                    if (!mPredictionList.isEmpty()) {
                        prediction_list_txt_title.setVisibility(View.GONE);
                        //Reverse list index to get latest prediction
                        Collections.reverse(mPredictionList);
                        patientPredictionAdapter = new PredictionAdapter(context,
                            mPredictionList,
                            1, //goToScreen 1: prediction result screen
                            mPredictionList.size());
                        prediction_list_recycler_view_main.setAdapter(patientPredictionAdapter);
                    } else {
                        prediction_list_txt_title.setVisibility(View.VISIBLE);
                    }

                    // Stop and hide shimmer
                    prediction_shimmer.stopShimmer();
                    prediction_shimmer.setVisibility(View.GONE);
                    // Display recycler view
                    prediction_list_recycler_view_main.setVisibility(View.VISIBLE);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "loadAllPredictionOfAccount()");
        }
    }
}