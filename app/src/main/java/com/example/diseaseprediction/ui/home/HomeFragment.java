package com.example.diseaseprediction.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diseaseprediction.AppConstants;
import com.example.diseaseprediction.Chat;
import com.example.diseaseprediction.Login;
import com.example.diseaseprediction.MainActivity;
import com.example.diseaseprediction.R;
import com.example.diseaseprediction.adapter.ConsultationAdapter;
import com.example.diseaseprediction.adapter.PredictionAdapter;
import com.example.diseaseprediction.firebase.FirebaseConstants;
import com.example.diseaseprediction.object.DoctorInfo;
import com.example.diseaseprediction.object.Message;
import com.example.diseaseprediction.object.Prediction;
import com.example.diseaseprediction.object.Session;
import com.example.diseaseprediction.ui.consultation.ConsultationListFragment;
import com.example.diseaseprediction.ui.prediction.PredictionListFragment;
import com.example.diseaseprediction.ui.predictionListConfirm.PredictionListPending;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.navigation.NavigationView;
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
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private DatabaseReference mRef;
    private FirebaseUser fUser;
    private DoctorInfo mDoctor;

    private String sessionID;

    private List<Session> consultationLists;
    private List<Prediction> mPredictionListDoctor;
    private List<Prediction> mPredictionListPatient;
    private ConsultationAdapter consultationAdapter;
    private PredictionAdapter doctorPredictionPendingListAdapter;
    private PredictionAdapter patientPredictionAdapter;

    private TextView home_txt_prediction_see_more, home_txt_consultation_see_more,
        home_doctor_all_prediction_txt_see_more,
        home_txt_title, home_txt_prediction_title, home_txt_consultation_title,
        home_doctor_all_prediction_no_prediction_title, home_prediction_no_prediction_title,
        home_consultation_no_consultation_title;
    private RelativeLayout home_doctor_all_prediction_layout_title, home_layout_disease_history;
    private LinearLayout home_search_view;
    private NavigationView navigationView;
    private RecyclerView home_recycler_view_consultation, home_recycler_view_disease,
        home_doctor_all_prediction_recycle_view;
    private ShimmerFrameLayout home_shimmer_pending_prediction, home_shimmer_prediction, home_shimmer_consultation;

    private Context context;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * IMPORTANT: all Context-related methods must be placed in here
     */
    @Override
    public void onViewCreated(@NonNull @NotNull View view,
                              @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getActivity().getApplicationContext();
        home_recycler_view_consultation.setLayoutManager(new LinearLayoutManager(context));
        home_recycler_view_disease.setLayoutManager(new LinearLayoutManager(context));
        //Search clicked
        home_search_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createSessionWithChatBot();
            }
        });


        //Set UI by role
        setUIByAccountType();

        //Load list consultation to recycler
        loadConsultationList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        //Get current user
        fUser = FirebaseAuth.getInstance().getCurrentUser();

        //set toolbar
        ((MainActivity) getActivity()).setActionBarTitle("");
        ((MainActivity) getActivity()).setIconToolbar();
        container.removeAllViews();

        //find view
        findViews(view);


        home_doctor_all_prediction_txt_see_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigationView.getMenu().getItem(2).setChecked(true);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,
                    new PredictionListPending()).commit();
            }
        });

        //See more prediction clicked
        home_txt_prediction_see_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigationView.getMenu().getItem(3).setChecked(true);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,
                    new PredictionListFragment()).commit();
            }
        });

        //See more consultation clicked
        home_txt_consultation_see_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigationView.getMenu().getItem(4).setChecked(true);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,
                    new ConsultationListFragment()).commit();

            }
        });


        return view;
    }

    /**
     * Find view by ID
     *
     * @param view view
     */
    private void findViews(View view) {
        try {
            navigationView = getActivity().findViewById(R.id.nav_view);
            home_txt_prediction_see_more = view.findViewById(R.id.home_txt_prediction_see_more);
            home_txt_consultation_see_more = view.findViewById(R.id.home_txt_consultation_see_more);
            home_doctor_all_prediction_txt_see_more = view.findViewById(R.id.home_doctor_all_prediction_txt_see_more);
            home_search_view = view.findViewById(R.id.home_search_view);

            home_txt_title = view.findViewById(R.id.home_txt_title);
            home_txt_prediction_title = view.findViewById(R.id.home_txt_prediction_title);
            home_txt_consultation_title = view.findViewById(R.id.home_txt_consultation_title);

            home_doctor_all_prediction_no_prediction_title =
                view.findViewById(R.id.home_doctor_all_prediction_no_prediction_title);
            home_prediction_no_prediction_title = view.findViewById(R.id.home_prediction_no_prediction_title);
            home_consultation_no_consultation_title = view.findViewById(R.id.home_consultation_no_consultation_title);

            //shimmer
            home_shimmer_pending_prediction = view.findViewById(R.id.home_shimmer_pending_prediction);
            home_shimmer_prediction = view.findViewById(R.id.home_shimmer_prediction);
            home_shimmer_consultation = view.findViewById(R.id.home_shimmer_consultation);
            // start shimmer
            home_shimmer_pending_prediction.startShimmer();
            home_shimmer_prediction.startShimmer();
            home_shimmer_consultation.startShimmer();

            //Create disease recycle
            home_recycler_view_disease = view.findViewById(R.id.home_recycler_view_disease);
            home_recycler_view_disease.setHasFixedSize(true);


            // Create pending prediction
            home_doctor_all_prediction_layout_title = view.findViewById(R.id.home_doctor_all_prediction_layout_title);
            home_doctor_all_prediction_recycle_view = view.findViewById(R.id.home_doctor_all_prediction_recycle_view);

            home_layout_disease_history = view.findViewById(R.id.home_layout_disease_history);

            //Create consultation recycle
            home_recycler_view_consultation = view.findViewById(R.id.home_recycler_view_consultation);
            home_recycler_view_consultation.setHasFixedSize(true);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "findView()");
        }
    }

    /**
     * Create new chat session
     * accountIDOne is sender
     * accountIDTwo is receiver
     * CHATBOT_ID is receiver (account two in consultation)
     */
    private void createSessionWithChatBot() {
        try {
            //Get consultation list of two account
            String accountIDOne;
            String accountIDTwo;
            if (fUser.getUid().compareTo(AppConstants.CHATBOT_ID) < 0) {
                accountIDOne = fUser.getUid();
                accountIDTwo = AppConstants.CHATBOT_ID;
            } else {
                accountIDOne = AppConstants.CHATBOT_ID;
                accountIDTwo = fUser.getUid();
            }

            mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_SESSION);
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean isFound = false;
                    Session ss = new Session();
                    // Find opening session
                    for (DataSnapshot sn : snapshot.getChildren()) {
                        ss = sn.getValue(Session.class);

                        //Check to find consultation is exist -> open Chat activity
                        if (ss != null &&
                            ss.getAccountIDOne().equals(accountIDOne) &&
                            ss.getAccountIDTwo().equals(accountIDTwo) &&
                            ss.getStatus() == 1) {
                            isFound = true;
                            //Send session id
                            Intent i = new Intent(getActivity(), Chat.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.putExtra("receiverID", AppConstants.CHATBOT_ID);
                            i.putExtra("sessionID", ss.getSessionID());
                            context.startActivity(i);
                        }
                    }
                    // If no open session found -> create new session and send welcome msg then open Chat activity
                    if (!isFound) {
                        mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_SESSION);
                        sessionID = mRef.push().getKey();
                        Session session = new Session(sessionID, new Date(), new Date(), 1);
                        session.setAccOneAndAccTwo(fUser.getUid(), AppConstants.CHATBOT_ID);
                        mRef.child(sessionID).setValue(session);

                        //Send message started
                        DatabaseReference reference =
                            FirebaseDatabase.getInstance().getReference().child(FirebaseConstants.FIREBASE_TABLE_MESSAGE + "/" + sessionID);
                        // Send message 1
                        Message msg = new Message(reference.push().getKey(), AppConstants.CHATBOT_ID,
                            context.getString(R.string.chat_chatbot_hello, AppConstants.CHATBOT_NAME)
                            , new Date(), sessionID, 1);
                        reference.child(msg.getMessageID()).setValue(msg);
                        // Send message 2
                        Message msg2 = new Message(reference.push().getKey(), AppConstants.CHATBOT_ID,
                            context.getString(R.string.chat_chatbot_ask, AppConstants.CHATBOT_NAME,
                                AppConstants.CHATBOT_NAME)
                            , new Date(), sessionID, 1);
                        reference.child(msg2.getMessageID()).setValue(msg2);

                        // Start Chat activity
                        Intent i = new Intent(getActivity(), Chat.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.putExtra("receiverID", AppConstants.CHATBOT_ID);
                        i.putExtra("sessionID", sessionID);
                        context.startActivity(i);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "createSessionWithChatBot()");
        }

    }

    /**
     * Load consultation of current account
     */
    private void loadConsultationList() {
        try {
            consultationLists = new ArrayList<>();
            mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_SESSION);
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    consultationLists.clear();
                    for (DataSnapshot sh : snapshot.getChildren()) {
                        Session ss = sh.getValue(Session.class);
                        if (ss.getAccountIDOne().equals(fUser.getUid()) || ss.getAccountIDTwo().equals(fUser.getUid())) {
                            consultationLists.add(ss);
                        }
                    }
                    // If list not empty
                    if (!consultationLists.isEmpty()) {
                        // Update UI
                        home_consultation_no_consultation_title.setVisibility(View.GONE);
                        // Display 'See more' button if list size greater than the defined number
                        if (consultationLists.size() > AppConstants.HOME_NUM_ITEMS_CONSULTATION) {
                            home_txt_consultation_see_more.setVisibility(View.VISIBLE);
                        }
                        //Reverse list index to get latest consultation
                        Collections.reverse(consultationLists);

                        consultationAdapter = new ConsultationAdapter(context, consultationLists,
                            AppConstants.HOME_NUM_ITEMS_CONSULTATION);
                        home_recycler_view_consultation.setAdapter(consultationAdapter);
                    } else {
                        home_consultation_no_consultation_title.setVisibility(View.VISIBLE);
                        home_txt_consultation_see_more.setVisibility(View.GONE);
                    }

                    // stop and hide shimmer
                    home_shimmer_consultation.stopShimmer();
                    home_shimmer_consultation.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "loadConsultationList()");
        }
    }

    /**
     * DOCTOR TYPE
     * Load all pending prediction
     */
    private void loadAllPendingPrediction() {
        try {
            mPredictionListDoctor = new ArrayList<>();
            //Find specialization id of doctor account
            mRef =
                FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_DOCTOR_INFO).child(fUser.getUid());
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mDoctor = snapshot.getValue(DoctorInfo.class);
                    //Go to prediction
                    Query predictionByDateCreate =
                        FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_PREDICTION).orderByChild("dateCreate/time");
                    predictionByDateCreate.addValueEventListener(new ValueEventListener() {
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
                                    Log.e(TAG, "Home. Patient ID null", e);
                                }
                            }
                            //Reverse list index to get latest consultation
//                        Collections.reverse(mPredictionListDoctor);
                            // if list not empty
                            if (!mPredictionListDoctor.isEmpty()) {
                                // Update UI
                                home_doctor_all_prediction_no_prediction_title.setVisibility(View.GONE);
                                // Display 'See more' button if list size greater than the defined number
                                if (mPredictionListDoctor.size() > AppConstants.HOME_NUM_ITEMS_PENDING_PREDICTION) {
                                    home_doctor_all_prediction_txt_see_more.setVisibility(View.VISIBLE);
                                }
                                // Load list to adapter
                                doctorPredictionPendingListAdapter = new PredictionAdapter(context,
                                    mPredictionListDoctor,
                                    0,   //goToScreen 0: doctor confirm screen
                                    AppConstants.HOME_NUM_ITEMS_PENDING_PREDICTION);
                                home_doctor_all_prediction_recycle_view.setAdapter(doctorPredictionPendingListAdapter);
                            } else {
                                home_doctor_all_prediction_no_prediction_title.setVisibility(View.VISIBLE);
                                home_doctor_all_prediction_txt_see_more.setVisibility(View.GONE);
                            }
                            // stop and hide shimmer
                            home_shimmer_pending_prediction.stopShimmer();
                            home_shimmer_pending_prediction.setVisibility(View.GONE);
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

    /**
     * Load prediction by type account
     * Load list prediction of patient if account type is 1
     * Load list prediction that confirmed by doctor if account type is 0
     *
     * @param typeAcc type of account. 0: doctor | 1: patient
     */
    private void loadAllPredictionOfAccount(int typeAcc) {
        try {
            mPredictionListPatient = new ArrayList<>();
            mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_PREDICTION);
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mPredictionListPatient.clear();
                    for (DataSnapshot sh : snapshot.getChildren()) {
                        Prediction pr = sh.getValue(Prediction.class);
                        //If patientId equal with current accountID
                        try {
                            // doctor
                            if (typeAcc == 0) {
                                if (pr.getDoctorID().equals(fUser.getUid())) {
                                    mPredictionListPatient.add(pr);
                                }
                            } else if (typeAcc == 1) { // user
                                if (pr.getPatientID().equals(fUser.getUid())) {
                                    mPredictionListPatient.add(pr);
                                }
                            }
                        } catch (NullPointerException e) {
                            Log.d(TAG, "Home. Patient ID null", e);
                        }
                    }

                    // if list not empty
                    if (!mPredictionListPatient.isEmpty()) {
                        //Update UI
                        home_prediction_no_prediction_title.setVisibility(View.GONE);
                        // Display 'See more' button if list size greater than the defined number
                        if (mPredictionListPatient.size() > AppConstants.HOME_NUM_ITEMS_PREDICTION) {
                            home_txt_prediction_see_more.setVisibility(View.VISIBLE);
                        }
                        //Reverse list index to get latest prediction
                        Collections.reverse(mPredictionListPatient);
                        // Load list to adapter
                        patientPredictionAdapter = new PredictionAdapter(context,
                            mPredictionListPatient,
                            1,  //goToScreen 1: prediction result screen
                            AppConstants.HOME_NUM_ITEMS_PREDICTION);

                        home_recycler_view_disease.setAdapter(patientPredictionAdapter);
                    } else if (typeAcc == 1) { // display prediction list if account is user
                        home_prediction_no_prediction_title.setVisibility(View.VISIBLE);
                        home_txt_prediction_see_more.setVisibility(View.GONE);
                    }
                    // stop and hide shimmer
                    home_shimmer_prediction.stopShimmer();
                    home_shimmer_prediction.setVisibility(View.GONE);
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

    /**
     * UI of doctor role
     */
    private void setUIDoctor() {
        try {
            home_txt_title.setText(context.getString(R.string.home_doctor_txt_title));
            home_txt_prediction_title.setText(context.getString(R.string.home_doctor_txt_prediction_title));
            home_txt_consultation_title.setText(context.getString(R.string.home_doctor_txt_consultation_title));

            //List disease history
            home_layout_disease_history.setVisibility(View.GONE);
            home_recycler_view_disease.setVisibility(View.GONE);
            home_shimmer_prediction.setVisibility(View.GONE);
            home_search_view.setVisibility(View.GONE);

            //All prediction in pending
            home_doctor_all_prediction_layout_title.setVisibility(View.VISIBLE);
            home_doctor_all_prediction_recycle_view.setVisibility(View.VISIBLE);
            home_shimmer_pending_prediction.setVisibility(View.VISIBLE);

            home_doctor_all_prediction_recycle_view.setHasFixedSize(true);
            home_doctor_all_prediction_recycle_view.setLayoutManager(new LinearLayoutManager(context));
            loadAllPendingPrediction();
            //type 0: doctor
            loadAllPredictionOfAccount(0);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "setUIDoctor()");
        }
    }

    /**
     * UI of user role
     */
    private void setUIPatient() {
        try {
            //Set doctor visibility gone
            home_doctor_all_prediction_layout_title.setVisibility(View.GONE);
            home_doctor_all_prediction_recycle_view.setVisibility(View.GONE);

            //Patient
            home_txt_title.setText(context.getString(R.string.home_txt_title));
            home_txt_prediction_title.setText(context.getString(R.string.home_txt_prediction_title));
            home_txt_consultation_title.setText(context.getString(R.string.home_txt_consultation_title));
            //type 1: patient
            loadAllPredictionOfAccount(1);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "setUIPatient()");
        }
    }

    /**
     * Set UI by type of account
     */
    private void setUIByAccountType() {
        try {
            //Get disease
            if (!fUser.getUid().equals("")) {
                mRef =
                    FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_ACCOUNT).child(fUser.getUid());
                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            if (snapshot.child("typeID").getValue().toString().equals("0")) {
                                setUIDoctor();
                            } else {
                                setUIPatient();
                            }

                        } catch (NullPointerException e) {
                            Log.e(TAG, "setUIByAccountType: TypeID Null", e);
                            Toast.makeText(context, context.getString(R.string.error_unknown_relogin),
                                Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();
                            Intent i = new Intent(context, Login.class);
                            startActivity(i);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "setUIByAccountType()");
        }
    }

}