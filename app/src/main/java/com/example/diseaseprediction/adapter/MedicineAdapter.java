package com.example.diseaseprediction.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diseaseprediction.AppConstants;
import com.example.diseaseprediction.R;
import com.example.diseaseprediction.firebase.FirebaseConstants;
import com.example.diseaseprediction.object.Prediction;
import com.example.diseaseprediction.object.PredictionMedicine;
import com.example.diseaseprediction.object.SymptomMedicine;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.ViewHolder> {
    private static final String LOG_TAG = "MedicineAdapter";
    private DatabaseReference mRef;
    private FirebaseUser fUser;
    private Context mContext;
    private List<PredictionMedicine> pmList;
    private SimpleDateFormat sdf;
    private int goToScreen;

    /**
     * @param context    Context
     * @param pmList     List of symptom medicine
     * @param goToScreen 0: Screen doctor confirm | 1: Screen prediction result
     * @param sizeLoad   size of prediction list
     */
    public MedicineAdapter(Context context, List<PredictionMedicine> pmList, int goToScreen, int sizeLoad) {
        try {
            fUser = FirebaseAuth.getInstance().getCurrentUser();

            this.mContext = context;
            this.goToScreen = goToScreen;

            //Limit row load
            this.pmList = new ArrayList<>();
//            this.pmList.add(new PredictionMedicine("gsagsagsags","kfhsaksagsa",1));

            if (sizeLoad >= pmList.size()) {
                this.pmList = pmList;
            } else {
                for (int i = 0; i < sizeLoad; i++) {
                    this.pmList.add(pmList.get(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "MedicineAdapter()");
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.item_prediction, parent, false);
        return new MedicineAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MedicineAdapter.ViewHolder holder, int position) {
        PredictionMedicine sm = pmList.get(position);
        getDataToUI(sm, holder.item_medicine_txt_name, holder.item_medicine_txt_dosage);

        // set default layout
        holder.medicine_item_divider.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return pmList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout item_prediction_img_main;
        public TextView item_medicine_txt_name, item_medicine_txt_dosage;
        public View medicine_item_divider;

        public ViewHolder(@NonNull View view) {
            super(view);
            item_medicine_txt_name = view.findViewById(R.id.item_medicine_txt_name);
            item_medicine_txt_dosage = view.findViewById(R.id.item_medicine_txt_dosage);
            medicine_item_divider = view.findViewById(R.id.medicine_item_divider);
        }

    }

    /**
     * Get data from firebase and set to UI
     *
     * @param predictionMedicine       current prediction medicine
     * @param item_medicine_txt_name   TextView that displays medicine name
     * @param item_medicine_txt_dosage TextView that displays medicine dosage
     */
    public void getDataToUI(PredictionMedicine predictionMedicine, TextView item_medicine_txt_name,
                            TextView item_medicine_txt_dosage) {
        try {
            //Get disease
            mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_PREDICTION_MEDICINE).child(predictionMedicine.getMedicineID());
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    item_medicine_txt_name.setText(snapshot.child("name").getValue().toString());
                    item_medicine_txt_dosage.setText(snapshot.child("dosage").getValue().toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "getDataToUI()");
        }
    }
}
