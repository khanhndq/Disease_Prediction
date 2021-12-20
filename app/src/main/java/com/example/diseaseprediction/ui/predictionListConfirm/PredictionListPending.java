package com.example.diseaseprediction.ui.predictionListConfirm;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diseaseprediction.MainActivity;
import com.example.diseaseprediction.R;
import com.example.diseaseprediction.adapter.PredictionAdapter;
import com.example.diseaseprediction.firebase.FirebaseConstants;
import com.example.diseaseprediction.object.DoctorInfo;
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
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class PredictionListPending extends Fragment {
    private static final String TAG = "PredictionListConfirm";

    private FirebaseUser fUser;
    private DatabaseReference mRef;
    private TextView prediction_list_confirm_txt_title;
    private RecyclerView prediction_list_confirm_recycler_view_main;
    private List<Prediction> mPredictionListDoctor;
    private PredictionAdapter doctorPredictionPendingListAdapter;
    private ShimmerFrameLayout prediction_confirm_shimmer;

    private DoctorInfo mDoctor;


    private Context context;

    public PredictionListPending() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view,
                              @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        context = getActivity().getApplicationContext();

        prediction_list_confirm_recycler_view_main.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        //Load UI
        loadAllPredictionPending();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        container.removeAllViews();

        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.menu_predictionList_pending));
        ((MainActivity) getActivity()).setIconToolbar();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_prediction_list_confirm, container, false);

        //Get current user
        fUser = FirebaseAuth.getInstance().getCurrentUser();

        prediction_list_confirm_txt_title = view.findViewById(R.id.prediction_list_confirm_txt_title);
        prediction_confirm_shimmer = view.findViewById(R.id.prediction_confirm_shimmer);
        prediction_confirm_shimmer.startShimmer();

        prediction_list_confirm_recycler_view_main = view.findViewById(R.id.prediction_list_confirm_recycler_view_main);
        prediction_list_confirm_recycler_view_main.setHasFixedSize(true);


        return view;
    }

    /**
     * DOCTOR TYPE
     * Load all prediction pending
     */
    private void loadAllPredictionPending() {
        try {
            mPredictionListDoctor = new ArrayList<>();
            //Find specialization id of doctor account
            mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_DOCTOR_INFO).child(fUser.getUid());
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mDoctor = snapshot.getValue(DoctorInfo.class);
                    //Go to prediction
                    Query predictionByDateCreate =
                            FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_PREDICTION).orderByChild("dateCreate/time");
                    predictionByDateCreate.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            mPredictionListDoctor.clear();
                            for (DataSnapshot sh : snapshot.getChildren()) {
                                Prediction pr = sh.getValue(Prediction.class);
                                assert pr != null;
                                try {
                                    if (pr.getStatus() == 0) {
                                        //Check if specializationID in prediction equal with specializationID in doctor
                                        // account
                                        if (pr.getHiddenSpecializationID().equals(mDoctor.getSpecializationID())) {
                                            //Add to prediction list
                                            mPredictionListDoctor.add(pr);
                                        }
                                    }
                                } catch (NullPointerException e) {
                                    Log.d(TAG, "Home. Patient ID null", e);
                                }
                            }
                            //Create adapter
                            // if list not empty
                            if (!mPredictionListDoctor.isEmpty()) {
                                prediction_list_confirm_txt_title.setVisibility(View.GONE);
                                //Reverse list index to get latest prediction
//                            Collections.reverse(mPredictionListDoctor);
                                doctorPredictionPendingListAdapter = new PredictionAdapter(context,
                                    mPredictionListDoctor,
                                    0, //goToScreen 0: doctor confirm screen
                                    mPredictionListDoctor.size());
                                prediction_list_confirm_recycler_view_main.setAdapter(doctorPredictionPendingListAdapter);
                            } else {
                                prediction_list_confirm_txt_title.setVisibility(View.VISIBLE);
                            }

                            // Stop and hide shimmer
                            prediction_confirm_shimmer.stopShimmer();
                            prediction_confirm_shimmer.setVisibility(View.GONE);

                            // Display recycler view
                            prediction_list_confirm_recycler_view_main.setVisibility(View.VISIBLE);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "loadAllPredictionPending()");
        }
    }
}