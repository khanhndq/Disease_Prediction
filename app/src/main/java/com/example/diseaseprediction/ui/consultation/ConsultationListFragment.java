package com.example.diseaseprediction.ui.consultation;

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
import com.example.diseaseprediction.adapter.ConsultationAdapter;
import com.example.diseaseprediction.firebase.FirebaseConstants;
import com.example.diseaseprediction.object.Session;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
public class ConsultationListFragment extends Fragment {
    private static final String TAG = "ConsultationFragment";
    private FirebaseUser fUser;
    private DatabaseReference mRef;

    private RecyclerView consultation_list_recycler_view_main;
    private ConsultationAdapter consultationAdapter;
    private List<Session> consultationLists;
    private TextView consultation_list_txt_title;
    private ShimmerFrameLayout consultation_shimmer;

    private Context context;

    public ConsultationListFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            //Set toolbar
            ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.menu_consultationList));
            ((MainActivity) getActivity()).setIconToolbar();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "getMessagesFirebase()");
        }
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view,
                              @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getActivity().getApplicationContext();


        consultation_list_recycler_view_main.setLayoutManager(new LinearLayoutManager(context));
        //Load user to recycler
        loadListConsultation();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Remove all view current in activity
        container.removeAllViews();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_consultation_list, container, false);

        //Get current user
        fUser = FirebaseAuth.getInstance().getCurrentUser();

        consultation_list_txt_title = view.findViewById(R.id.consultation_list_txt_title);
        consultation_shimmer = view.findViewById(R.id.consultation_shimmer);
        consultation_shimmer.startShimmer();

        //Create recycler
        consultation_list_recycler_view_main = view.findViewById(R.id.consultation_list_recycler_view_main);
        consultation_list_recycler_view_main.setHasFixedSize(true);
        consultationLists = new ArrayList<>();


        return view;
    }

    /**
     * Load all consultation list base on user ID
     */
    private void loadListConsultation() {
        try {
            mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_SESSION);
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    consultationLists.clear();
                    for (DataSnapshot sh : snapshot.getChildren()) {
                        Session ss = sh.getValue(Session.class);
                        //Get all consultation list depend on user ID 1 or user ID 2
                        if (ss.getAccountIDOne().equals(fUser.getUid()) || ss.getAccountIDTwo().equals(fUser.getUid())) {
                            consultationLists.add(ss);
                        }
                    }
                    // If list not empty
                    if (!consultationLists.isEmpty()) {
                        consultation_list_txt_title.setVisibility(View.GONE);
                        //Reverse list index to get latest consultation
                        Collections.reverse(consultationLists);
                        consultationAdapter = new ConsultationAdapter(context, consultationLists,
                            consultationLists.size());
                        consultation_list_recycler_view_main.setAdapter(consultationAdapter);
                    } else {
                        // display empty string
                        consultation_list_txt_title.setVisibility(View.VISIBLE);
                    }

                    // Stop and hide shimmer
                    consultation_shimmer.stopShimmer();
                    consultation_shimmer.setVisibility(View.GONE);
                    // display recycler view
                    consultation_list_recycler_view_main.setVisibility(View.VISIBLE);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "loadListConsultation()");
        }
    }
}