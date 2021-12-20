package com.example.diseaseprediction;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.diseaseprediction.firebase.FirebaseConstants;
import com.example.diseaseprediction.listener.NetworkChangeListener;
import com.example.diseaseprediction.object.Disease;
import com.example.diseaseprediction.object.Medicine;
import com.example.diseaseprediction.object.MedicineType;
import com.example.diseaseprediction.object.Message;
import com.example.diseaseprediction.object.Prediction;
import com.example.diseaseprediction.object.PredictionMedicine;
import com.google.android.material.textfield.TextInputLayout;
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

public class PredictionConfirm extends AppCompatActivity {

    private static final String LOG_TAG = "Prediction Confirm";
    //Internet connection
    private NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    //
    private ArrayList<Disease> diseasesList = new ArrayList<>();
    private ArrayList<Medicine> loadMedicineList = new ArrayList<>();
    private ArrayList<MedicineType> loadMedicineTypeList = new ArrayList<>();
    private ArrayList<PredictionMedicine> predictionMedicineList = new ArrayList<>();
    // Firebase
    private DatabaseReference mRef;
    private FirebaseUser fUser;
    // Layout
    private ImageView prediction_confirm_toolbar_img_pre;
    private TextView prediction_confirm_txt_disease_description_result;
    private TextView prediction_confirm_txt_disease_prediction_result;
    private TextInputLayout prediction_confirm_disease_select_layout, prediction_confirm_disease_other_layout;
    private AutoCompleteTextView prediction_confirm_disease_select;
    private EditText prediction_confirm_disease_other;
    private Button prediction_confirm_prediction_wrong_btn, prediction_confirm_prediction_correct_btn,
            prediction_confirm_prediction_confirm_btn;
    private TextView prediction_confirm_txt_medicine_name, medicine_confirm_instruction_txt, prediction_txt_medicine_empty;
    private TextView prediction_confirm_txt_medicine_dosage, medicine_confirm_img_add;
    private LinearLayout medicine_confirm_layout, medicine_confirm_layout_add_list;
    private View item_medicine_view;
    private TextView medicineName;
    private TextView medicineDosage;
    private LinearLayout medicine_confirm_layout_title;

    private ArrayAdapter diseaseAdapter;
    private Prediction mPrediction;
    private int predictionStatus;
    private Disease selectedDisease; // currently selected disease in combo box
    private boolean isMedicineEmpty = true;
    private int isInstructionEmpty = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction_confirm);

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        // Get prediction ID from Intent
        Intent intent = getIntent();
        mPrediction = intent.getParcelableExtra("mPrediction");
        setUpUI();
        getPredictionMedicine();
        checkPredictionStatus();
        getPredictionMedicineToUI(mPrediction.getPredictionID());
    }

    @Override
    protected void onStart() {
        //Check internet connected or not
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        //Check internet connected or not
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }

    /**
     * Set up necessary elements for the UI
     */
    private void setUpUI() {
        try {
            getViews();

            getDiseasesListAndLoadUI();

            loadPatientDescription();

            // Event for Incorrect button
            prediction_confirm_prediction_wrong_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    displayConfirmDialog(0);
                }
            });
            // Event for correct button
            prediction_confirm_prediction_correct_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Check if disease is equal to other_disease
                    if (mPrediction.getDiseaseID().equals(AppConstants.DISEASE_OTHER_ID)) {
                        displayWarningDialog(getString(R.string.default_other_disease_confirm));
                    } else {
                        //Check if medicine is empty and instruction is default
                        if (!isMedicineEmpty && isInstructionEmpty != 0) {
                            displayWarningDialog(getString(R.string.default_empty_instruction));
                        } else {
                            displayConfirmDialog(1);
                        }
                    }
                }
            });
            // Event for confirm button
            prediction_confirm_prediction_confirm_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    handleSaveData();
                    if (handleSaveData() && checkPredictionMedicine()) {
                        displayConfirmEditDialog(fUser.getUid(), 1);
                    }
                }
            });
            // Event for back button
            prediction_confirm_toolbar_img_pre.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            // Event for Add new Medicine
            medicine_confirm_img_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Create default prediction medicine layout
                    getMedicineAndDosageToUI(AppConstants.MEDICINE_OTHER_ID, "1", AppConstants.MEDICINE_TYPE_DEFAULT);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "setUpUI()");
        }

    }

    /**
     * Get UI elements on View
     */
    private void getViews() {
        try {
            prediction_confirm_toolbar_img_pre = findViewById(R.id.prediction_confirm_toolbar_img_pre);

            prediction_confirm_txt_disease_description_result =
                    findViewById(R.id.prediction_confirm_txt_patient_description_txt);
            prediction_confirm_txt_disease_prediction_result =
                    findViewById(R.id.prediction_confirm_txt_disease_prediction_disease_name);

            prediction_confirm_disease_select_layout = findViewById(R.id.prediction_confirm_disease_select_layout);
            prediction_confirm_disease_select = findViewById(R.id.prediction_confirm_disease_select);

            prediction_confirm_disease_other_layout = findViewById(R.id.prediction_confirm_disease_other_layout);
            prediction_confirm_disease_other = findViewById(R.id.prediction_confirm_disease_other);

            prediction_confirm_prediction_wrong_btn = findViewById(R.id.prediction_confirm_prediction_wrong_btn);
            prediction_confirm_prediction_correct_btn = findViewById(R.id.prediction_confirm_prediction_correct_btn);
            prediction_confirm_prediction_confirm_btn = findViewById(R.id.prediction_confirm_prediction_confirm_btn);

            prediction_confirm_txt_medicine_name = findViewById(R.id.prediction_confirm_txt_medicine_name);
            prediction_confirm_txt_medicine_name.setText(R.string.prediction_confirm_txt_medicine_name);
            prediction_confirm_txt_medicine_dosage = findViewById(R.id.prediction_confirm_txt_medicine_dosage);
            prediction_confirm_txt_medicine_dosage.setText(R.string.prediction_confirm_txt_medicine_dosage);
            medicine_confirm_layout = findViewById(R.id.medicine_confirm_layout);
            medicine_confirm_layout.removeAllViews();
            medicine_confirm_img_add = findViewById(R.id.medicine_confirm_img_add);
            medicine_confirm_layout_add_list = findViewById(R.id.medicine_confirm_layout_add_list);
            medicine_confirm_layout_title = findViewById(R.id.medicine_confirm_layout_title);
            prediction_txt_medicine_empty = findViewById(R.id.prediction_txt_medicine_empty);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "getViews()");
        }

    }

    /**
     * Get disease list from Firebase and create adapter for {@link AutoCompleteTextView}. Then, load diseases name
     * into the Prediction Result Textview from Firebase and set default selection of hidden
     * {@link AutoCompleteTextView}
     */
    private void getDiseasesListAndLoadUI() {
        try {
            mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_DISEASE);
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    // Get diseases and add to list
                    for (DataSnapshot sn : snapshot.getChildren()) {
                        Disease disease = sn.getValue(Disease.class);
                        // if this disease is the predicted one -> save for later use
                        if (disease.getDiseaseID().equals(mPrediction.getDiseaseID())) {
                            selectedDisease = disease;
                        }
                        diseasesList.add(disease);
                    }

                    // create new ArrayAdapter
                    diseaseAdapter = new ArrayAdapter(PredictionConfirm.this,
                            R.layout.support_simple_spinner_dropdown_item,
                            diseasesList);
                    prediction_confirm_disease_select.setAdapter(diseaseAdapter);

                    // Set onItemClickListener to check for 'other disease', then set visibility of input layout
                    prediction_confirm_disease_select.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            selectedDisease = (Disease) parent.getItemAtPosition(position);
                            // selected disease is 'Other disease'
                            if (selectedDisease.getDiseaseID().equals(AppConstants.DISEASE_OTHER_ID)) {
                                prediction_confirm_disease_other_layout.setVisibility(View.VISIBLE);
                            } else {
                                prediction_confirm_disease_other_layout.setVisibility(View.GONE);
                            }
                        }
                    });

                    // Set disease name and default selected disease
                    prediction_confirm_txt_disease_prediction_result.setText(selectedDisease.getName());
                    prediction_confirm_disease_select.setText(selectedDisease.getName());
                    diseaseAdapter.getFilter().filter(null);
                    // if selected disease is "Other disease" -> display Incorrect layout for doctor to edit disease
                    if (selectedDisease.getDiseaseID().equals(AppConstants.DISEASE_OTHER_ID)) {
                        changeUIIncorrect();
                    }

                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "getDiseasesListAndLoadUI()");
        }
    }

    /**
     * Load Patient's symptoms from the chat between patient and the chat bot from Firebase.
     * After successfully loaded, these symptoms will be loaded on the TextView
     */
    private void loadPatientDescription() {
        try {
            ArrayList<Message> messagesList = new ArrayList<>();

            String sessionID = mPrediction.getSessionID();
            // get messages in session between user and chat bot
            mRef = FirebaseDatabase.getInstance()
                    .getReference(FirebaseConstants.FIREBASE_TABLE_MESSAGE + "/" + sessionID);

            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                    for (DataSnapshot sn : snapshot.getChildren()) {
                        Message message = sn.getValue(Message.class);
                        try {
                            // Get messages that was sent by patient
                            if (message.getSenderID().equals(mPrediction.getPatientID())) {
                                messagesList.add(message);
                            }
                        } catch (NullPointerException e) {
                            Log.e(LOG_TAG, "loadPatientDescription: Null pointer", e);
                            Toast.makeText(PredictionConfirm.this, getString(R.string.error_unknown_contactDev),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    displayMessagesList(messagesList);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "loadPatientDescription()");
        }
    }

    /**
     * Display messages from {@code messagesList} to UI
     *
     * @param messagesList an {@link ArrayList} that store {@link Message}
     */
    private void displayMessagesList(ArrayList<Message> messagesList) {
        try {
            String finalText = "";

            // iterate and concatenate string, then displays messages from messagesList to TextView
            for (Message message : messagesList) {
                finalText = finalText.concat("- " + message.getMessage() + "\n");
            }

            prediction_confirm_txt_disease_description_result.setText(finalText);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "displayMessagesList()");
        }
    }

    /**
     * Display confirm dialog when user click on buttons.<br/>  If user select positive button, depend on the dialog
     * type, the following action will be made:
     * <ol>
     * <li>Type 0: The method  {@link PredictionConfirm#changeUIIncorrect()} will be called </li>
     * <li>Type 1: the method {@link PredictionConfirm#savePrediction} will be called</li>
     * </ol>
     *
     * @param type type of dialog. 0 is for incorrect prediction, 1 is correct prediction
     */
    private void displayConfirmDialog(int type) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            // User select Incorrect btn
            if (type == 0) {
                // Set content
                builder.setMessage(R.string.prediction_confirm_dialog_incorrect);

                // Set button
                builder.setPositiveButton(R.string.dialog_button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        changeUIIncorrect();
                    }
                });

                builder.setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                });
                builder.create().show();
            } else if (type == 1) { // User select Correct btn
                // Set content
                builder.setMessage(R.string.prediction_confirm_dialog_correct);

                // Set button
                builder.setPositiveButton(R.string.dialog_button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Save prediction with new doctorID and updated status
                        String doctorID = fUser.getUid();
                        savePrediction(doctorID, 0);
                    }
                });

                builder.setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                });

                builder.create().show();
            } else { // Wrong argument pass in
                Log.w(LOG_TAG, "displayConfirmDialog: invalid 'type' argument!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "displayConfirmDialog()");
        }
    }

    /**
     * Change UI elements for Incorrect mode
     */
    private void changeUIIncorrect() {
        try {
            // display
            prediction_confirm_disease_select_layout.setVisibility(View.VISIBLE);
            prediction_confirm_prediction_confirm_btn.setVisibility(View.VISIBLE);

            medicine_confirm_img_add.setVisibility(View.VISIBLE);
            medicine_confirm_layout_add_list.setVisibility(View.VISIBLE);

            // If current disease is 'Other disease' -> display edit text for doctor to enter disease name
            if (selectedDisease.getDiseaseID().equals(AppConstants.MEDICINE_OTHER_ID)) {
                prediction_confirm_disease_other_layout.setVisibility(View.VISIBLE);
            }

            // hide
            prediction_confirm_prediction_correct_btn.setVisibility(View.GONE);
            prediction_confirm_prediction_wrong_btn.setVisibility(View.GONE);

            medicine_confirm_layout.setVisibility(View.GONE);
            medicine_confirm_layout_title.setVisibility(View.GONE);

            // set text
            prediction_confirm_txt_disease_prediction_result.setText(R.string.prediction_confirm_txt_disease_prediction_title);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "changeUIIncorrect()");
        }
    }

    /**
     * This method is built for Confirm button. It will check and save updated data to Firebase
     */
    private boolean handleSaveData() {
        boolean check = false;
        String note = "";
        String diseaseID = selectedDisease.getDiseaseID();
        // Check if doctor select other disease option
        if (diseaseID.equals(AppConstants.DISEASE_OTHER_ID)) {
            // get disease name
            note = prediction_confirm_disease_other.getText().toString();
            // check if disease name empty
            if (!note.trim().isEmpty()) {
//                savePrediction(fUser.getUid(), 1);
                check = true;
            } else {
                prediction_confirm_disease_other_layout.setError(getString(R.string.error_field_empty));
            }
        } else { // doctor select known diseases
//            savePrediction(fUser.getUid(), 1);
            check = true;
        }
        return check;
    }


    /**
     * Save prediction data. When prediction successfully saved, a {@link PredictionConfirm#displayThanksDialog()} will
     * be called
     *
     * @param doctorID doctor ID
     * @param type     type to save. <ul>
     *                 <li>0: correct</li>
     *                 <li>1: incorrect</li>
     *                 </ul>
     */
    private void savePrediction(String doctorID, int type) {
        try {
            // check if prediction status still equal to 0 i.e "waiting for confirmation"
            if (predictionStatus == 0) {
                mRef =
                        FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_PREDICTION).child(mPrediction.getPredictionID());
                mRef.child("doctorID").setValue(doctorID);
                // if prediction correct
                if (type == 0) {
                    mRef.child("status").setValue(1); // prediction correct
                } else if (type == 1) { // prediction incorrect
                    String diseaseName = prediction_confirm_disease_other.getText().toString();
                    mRef.child("diseaseID").setValue(selectedDisease.getDiseaseID()); //set correct disease ID
                    // if doctor select unknown disease -> disease name not empty
                    if (!diseaseName.isEmpty()) {
                        mRef.child("notes").setValue(diseaseName);
                    }
                    mRef.child("status").setValue(2); // prediction incorrect
                }
            }

            mRef.child("dateUpdate").setValue(new Date());

            displayThanksDialog();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "savePrediction()");
        }

    }

    /**
     * Check for prediction status constantly. If the status changed to 1 and prediction doctorID is not the current
     * user, display a dialog and send user back to Home
     */
    private void checkPredictionStatus() {
        try {
            mRef =
                    FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_PREDICTION).child(mPrediction.getPredictionID());
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Prediction prediction = snapshot.getValue(Prediction.class);
                    // if prediction is confirmed but not by current doctor
                    try {
                        if (prediction.getStatus() != 0 && !prediction.getDoctorID().equals(fUser.getUid())) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(PredictionConfirm.this);
                            builder.setMessage(R.string.prediction_confirm_dialog_prediction_status_invalid);
                            builder.setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // redirect to Home
                                    Intent intent = new Intent(PredictionConfirm.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            });
                            builder.create().show();
                        }
                    } catch (NullPointerException e) {
                        Log.e(LOG_TAG, "checkPredictionStatus: Param null", e);
                        Toast.makeText(PredictionConfirm.this, getString(R.string.error_unknown_contactDev),
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "checkPredictionStatus()");
        }
    }

    /**
     * Show dialog to confirm editing before saving
     */
    private void displayConfirmEditDialog(String doctorID, int type) {
        try {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.dialog_prediction_edit_confirm));
            builder.setPositiveButton(getString(R.string.dialog_prediction_edit_confirmed),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            savePredictionMedicine();
                            savePrediction(doctorID, type);
                        }
                    });
            builder.setNegativeButton(getString(R.string.dialog_prediction_edit_check_again),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
            builder.create().show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "displayConfirmEditDialog()");
        }
    }

    /**
     * A Thank you dialog to thanks doctor when they confirm the prediction. When doctor press on positive button, they
     * will be navigate to Home
     */
    private void displayThanksDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.prediction_confirm_dialog_thankyou);

            builder.setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Navigation to Home
                    Intent intent = new Intent(PredictionConfirm.this, MainActivity.class);
                    startActivity(intent);
                    finish(); //clear this activity from back stack
                }
            });

            builder.create().show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "displayThanksDialog()");
        }
    }

    /**
     * Create a single positive button dialog for warning
     *
     * @param message warning message
     */
    private void displayWarningDialog(String message) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(message);

            builder.setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builder.create().show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "displayWarningDialog()");
        }
    }

    /**
     * Get Prediction Medicine
     */
    private void getPredictionMedicine() {
        try {
            Query QGetPredictionMedicine = FirebaseDatabase.getInstance()
                    .getReference(FirebaseConstants.FIREBASE_TABLE_PREDICTION_MEDICINE)
                    .orderByChild("predictionID").equalTo(mPrediction.getPredictionID());
            QGetPredictionMedicine.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    for (DataSnapshot sn : snapshot.getChildren()) {
                        PredictionMedicine pm = sn.getValue(PredictionMedicine.class);
                        getMedicine(pm.getMedicineID(), pm.getDosage(), pm.getNotes(), pm.getMedicineTypeID(),
                                pm.getInstruction());
                        predictionMedicineList.add(pm);
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "getPredictionMedicine()");
        }
    }

    /**
     * Get medicine
     * Then add medicine to layout medicine
     */
    private void getMedicine(String medicineID, String dosage, String notes, String medicineTypeID,
                             String instruction) {
        try {
            Query QGetMedicine = FirebaseDatabase.getInstance()
                    .getReference(FirebaseConstants.FIREBASE_TABLE_MEDICINE)
                    .orderByChild("medicineID").equalTo(medicineID);
            QGetMedicine.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    for (DataSnapshot sn : snapshot.getChildren()) {
                        Medicine m = sn.getValue(Medicine.class);
                        if(m.getStatus()!=0){
                            item_medicine_view = getLayoutInflater().inflate(R.layout.item_medicine_view, null, false);
                            medicineName = item_medicine_view.findViewById(R.id.item_medicine_txt_name);
                            medicineDosage = item_medicine_view.findViewById(R.id.item_medicine_txt_dosage);
                            medicine_confirm_instruction_txt = item_medicine_view.findViewById(R.id.medicine_confirm_instruction_txt);
                            if (medicineID.equals(AppConstants.MEDICINE_OTHER_ID)) {
                                medicineName.setText(notes);
                            } else {
                                medicineName.setText(m.getName());
                            }
                            getMedicineTypeByID(medicineTypeID, dosage, medicineDosage);
                            //Check if instruction equal to default, then show default_instruction message
                            if (instruction.equals("Default")) {
                                medicine_confirm_instruction_txt.setText(getString(R.string.default_instruction));
                                isInstructionEmpty++;
                            } else {
                                medicine_confirm_instruction_txt.setText(instruction);
                            }

                            medicineDosage.setText(dosage);
                            medicine_confirm_layout.addView(item_medicine_view);
                        }
                    }
                    try {
                        if (!medicineName.getText().toString().isEmpty()) {
                            isMedicineEmpty = false;
                            medicine_confirm_layout_title.setVisibility(View.VISIBLE);
                        } else {
                            isMedicineEmpty = true;
                            medicine_confirm_layout_title.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d(LOG_TAG, "getMedicine(), empty medicineName");
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "getMedicine()");
        }
    }

    /**
     * Get type of medicine by ID
     *
     * @param medicineTypeID medicine type
     * @param dosage         dosage
     * @param medicineDosage medicine dosage
     */
    private void getMedicineTypeByID(String medicineTypeID, String dosage, TextView medicineDosage) {
        try {
            //get medicine type
            Query QGetMedicineType = FirebaseDatabase.getInstance()
                    .getReference(FirebaseConstants.FIREBASE_TABLE_MEDICINE_TYPE)
                    .orderByChild("medicineTypeID").equalTo(medicineTypeID);
            QGetMedicineType.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    for (DataSnapshot sn : snapshot.getChildren()) {
                        MedicineType mt = sn.getValue(MedicineType.class);
                        medicineDosage.setText(dosage + " " + mt.getMedicineName());
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "getMedicineTypeByID()");
        }
    }

    /**
     * Check valid of medicine_confirm_layout_add_list
     */
    private boolean checkPredictionMedicine() {
        //Check valid data of UI (Is empty or not)
        if (checkMedicineLayout() == 0) {
            return true;
        }
        return false;
    }

    /**
     * Save predictionMedicine
     */
    private void savePredictionMedicine() {
        try {
            //Remove all prediction medicine in firebase
            removeAllPredictionMedicine(mPrediction.getPredictionID());
            //Then add new prediction medicine to firebase
            for (int i = 0; i < predictionMedicineList.size(); i++) {
                PredictionMedicine tmp = new PredictionMedicine(mPrediction.getPredictionID(),
                        predictionMedicineList.get(i).getMedicineID(),
                        predictionMedicineList.get(i).getDosage(), predictionMedicineList.get(i).getMedicineTypeID(),
                        predictionMedicineList.get(i).getNotes(), predictionMedicineList.get(i).getInstruction(), 1);
                addPredictionMedicine(tmp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "savePredicitonMedicine()");
        }
    }

    /**
     * Load prediction medicine to UI
     *
     * @param predictionID prediction ID
     */
    private void getPredictionMedicineToUI(String predictionID) {
        try {
            Query QGetPredictionMedicine = FirebaseDatabase.getInstance()
                    .getReference(FirebaseConstants.FIREBASE_TABLE_PREDICTION_MEDICINE)
                    .orderByChild("predictionID").equalTo(predictionID);
            QGetPredictionMedicine.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    for (DataSnapshot sn : snapshot.getChildren()) {
                        PredictionMedicine pm = sn.getValue(PredictionMedicine.class);
                        getMedicineAndDosageToUI(pm.getMedicineID(), pm.getDosage(), pm.getMedicineTypeID());
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "getPredictionMedicineToUI()");
        }
    }

    /**
     * Get medicine and dosage
     * Then load it to UI
     *
     * @param medicineID medicine ID
     * @param dosage     dosage
     */
    private void getMedicineAndDosageToUI(String medicineID, String dosage, String medicineTypeID) {
        try {
            Query QGetMedicine = FirebaseDatabase.getInstance()
                    .getReference(FirebaseConstants.FIREBASE_TABLE_MEDICINE)
                    .orderByChild("medicineID").equalTo(medicineID);
            QGetMedicine.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    for (DataSnapshot sn : snapshot.getChildren()) {
                        Medicine m = sn.getValue(Medicine.class);
                        if(m.getStatus()!=0){
                            addView(m, dosage, medicineTypeID);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "getMedicineAndDosageToUI()");
        }
    }


    /**
     * Add new row to medicine_confirm_layout_add_list
     *
     * @param mMedicine medicine
     * @param dosage    dosage
     */
    private void addView(Medicine mMedicine, String dosage, String medicineTypeID) {
        try {
            //Find view of layout
            final View item_add_medicine
                    = getLayoutInflater().inflate(R.layout.item_medicine_add, null, false);
            TextInputLayout item_add_medicine_editText_order_layout
                    = item_add_medicine.findViewById(R.id.item_add_medicine_editText_order_layout);
            LinearLayout item_add_medicine_linear_order_medicine
                    = item_add_medicine.findViewById(R.id.item_add_medicine_linear_order_medicine);
            TextInputLayout item_add_medicine_editText_dosage_layout
                    = item_add_medicine.findViewById(R.id.item_add_medicine_editText_dosage_layout);
            AutoCompleteTextView item_add_medicine_autoComplete
                    = item_add_medicine.findViewById(R.id.item_add_medicine_autoComplete);
            AutoCompleteTextView item_add_medicine_type_autoComplete
                    = item_add_medicine.findViewById(R.id.item_add_medicine_type_autoComplete);
            ImageView item_add_medicine_btn_delete
                    = item_add_medicine.findViewById(R.id.item_add_medicine_btn_delete);
            TextView hiddenMedicineValue
                    = item_add_medicine.findViewById(R.id.hiddenMedicineValue);
            TextView hiddenMedicineTypeValue
                    = item_add_medicine.findViewById(R.id.hiddenMedicineTypeValue);
            TextView item_medicine_txt_dosage_add = item_add_medicine.findViewById(R.id.item_medicine_txt_dosage_add);
            TextView item_medicine_txt_name_add = item_add_medicine.findViewById(R.id.item_medicine_txt_name_add);
            item_medicine_txt_name_add.setText(R.string.prediction_confirm_txt_medicine_name);
            item_medicine_txt_dosage_add.setText(R.string.prediction_confirm_txt_medicine_dosage);

            //Load list Medicine
            mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_MEDICINE);
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    //Clear medicine array
                    loadMedicineList.clear();
                    for (DataSnapshot sn : snapshot.getChildren()) {
                        Medicine medicine = sn.getValue(Medicine.class);
                        if(medicine.getStatus()!=0){
                            loadMedicineList.add(medicine);
                        }
                    }

                    // Reverse medicine list
                    Collections.reverse(loadMedicineList);

                    ArrayAdapter arrayAdapter = new ArrayAdapter(PredictionConfirm.this,
                            android.R.layout.simple_spinner_item, loadMedicineList);
                    item_add_medicine_autoComplete.setAdapter(arrayAdapter);

                    //Handle event item click AutoCompleteTextView
                    item_add_medicine_autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Medicine tmpM = (Medicine) adapterView.getItemAtPosition(i);
                            hiddenMedicineValue.setText(tmpM.getMedicineID());
                            if (item_add_medicine_autoComplete.getText().toString().equals(AppConstants.MEDICINE_OTHER_NAME)) {
                                item_add_medicine_linear_order_medicine.setVisibility(View.VISIBLE);
                            } else {
                                item_add_medicine_linear_order_medicine.setVisibility(View.GONE);
                            }
                        }
                    });

                    //Set data to UI
                    item_add_medicine_autoComplete.setText(mMedicine.getName());
                    arrayAdapter.getFilter().filter(null);
                    //IF value equal to MEDICINE_OTHER_NAME. Then set visible editText
                    if (item_add_medicine_autoComplete.getText().toString().equals(AppConstants.MEDICINE_OTHER_NAME)) {
                        item_add_medicine_linear_order_medicine.setVisibility(View.VISIBLE);
                    } else {
                        item_add_medicine_linear_order_medicine.setVisibility(View.GONE);
                    }
                    hiddenMedicineValue.setText(mMedicine.getMedicineID());
                    item_add_medicine_editText_dosage_layout.getEditText().setText(dosage);

                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
            //Load list Medicine Type
            mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_MEDICINE_TYPE);
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    //Clear medicine type array
                    loadMedicineTypeList.clear();
                    for (DataSnapshot sn : snapshot.getChildren()) {
                        MedicineType medicineType = sn.getValue(MedicineType.class);
                        loadMedicineTypeList.add(medicineType);
                    }

                    // Reverse medicine type list
                    Collections.reverse(loadMedicineTypeList);

                    ArrayAdapter arrayAdapter = new ArrayAdapter(PredictionConfirm.this,
                            android.R.layout.simple_spinner_item, loadMedicineTypeList);
                    item_add_medicine_type_autoComplete.setAdapter(arrayAdapter);

                    //Handle event item click AutoCompleteTextView
                    item_add_medicine_type_autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            MedicineType tmpM = (MedicineType) adapterView.getItemAtPosition(i);
                            hiddenMedicineTypeValue.setText(tmpM.getMedicineTypeID());

                        }
                    });
                    //Set data to UI
                    for (MedicineType mt : loadMedicineTypeList) {
                        if (mt.getMedicineTypeID().equals(medicineTypeID)) {
                            item_add_medicine_type_autoComplete.setText(mt.getMedicineName());
                            arrayAdapter.getFilter().filter(null);
                            hiddenMedicineTypeValue.setText(mt.getMedicineTypeID());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
            //Button X
            item_add_medicine_btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeView(item_add_medicine);
                }
            });
            //Add layout
            medicine_confirm_layout_add_list.addView(item_add_medicine);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "addView()");
        }
    }


    /**
     * Check data on medicine layout
     *
     * @return number of invalid checked
     */
    private int checkMedicineLayout() {
        //List prediction medicine
        predictionMedicineList.clear();
        int checkValid = 0;
        String otherMedicine = "";

        try {
            //Loop all layout
            for (int i = 0; i < medicine_confirm_layout_add_list.getChildCount(); i++) {
                PredictionMedicine prm = new PredictionMedicine(mPrediction.getPredictionID(), "Default", "Default",
                        AppConstants.MEDICINE_TYPE_DEFAULT, "Default", "Default", 1);
                //Get view on each layout
                View item_add_medicine = medicine_confirm_layout_add_list.getChildAt(i);
                //Find UI
                TextInputLayout item_add_medicine_editText_dosage_layout
                        = item_add_medicine.findViewById(R.id.item_add_medicine_editText_dosage_layout);
                TextInputLayout item_add_medicine_editText_order_layout
                        = item_add_medicine.findViewById(R.id.item_add_medicine_editText_order_layout);
                TextInputLayout medicine_confirm_instruction_edit_layout
                        = item_add_medicine.findViewById(R.id.medicine_confirm_instruction_edit_layout);
                AutoCompleteTextView item_add_medicine_autoComplete
                        = item_add_medicine.findViewById(R.id.item_add_medicine_autoComplete);
                TextView hiddenMedicineValue
                        = item_add_medicine.findViewById(R.id.hiddenMedicineValue);
                TextView hiddenMedicineTypeValue
                        = item_add_medicine.findViewById(R.id.hiddenMedicineTypeValue);

                //Add event clear error
                item_add_medicine_editText_dosage_layout.getEditText()
                        .addTextChangedListener(clearErrorOnTyping(item_add_medicine_editText_dosage_layout));
                item_add_medicine_editText_order_layout.getEditText()
                        .addTextChangedListener(clearErrorOnTyping(item_add_medicine_editText_order_layout));
                medicine_confirm_instruction_edit_layout.getEditText()
                        .addTextChangedListener(clearErrorOnTyping(medicine_confirm_instruction_edit_layout));
                //Check valid on each layout
                //If AutoCompleteTextView is other medicine
                if (item_add_medicine_autoComplete.getText().toString().equals(AppConstants.MEDICINE_OTHER_NAME)) {
                    otherMedicine = item_add_medicine_editText_order_layout.getEditText().getText().toString();
                    //Check if other_medicine_editText empty
                    if (!otherMedicine.trim().isEmpty()) {
                        //Check editText dosage
                        if (!item_add_medicine_editText_dosage_layout.getEditText().getText().toString().equals("")) {
                            prm.setDosage(item_add_medicine_editText_dosage_layout.getEditText().getText().toString());
                            prm.setMedicineID(hiddenMedicineValue.getText().toString());
                            //Add note that other medicine name
                            prm.setNotes(otherMedicine);
                            prm.setMedicineTypeID(hiddenMedicineTypeValue.getText().toString());
                        }
                    } else {
                        //Other medicine empty
                        item_add_medicine_editText_order_layout.setError(getString(R.string.error_field_empty));
                        checkValid++;
                    }
                }
                //Check dosage empty
                if (!item_add_medicine_editText_dosage_layout.getEditText().getText().toString().equals("")) {
                    prm.setDosage(item_add_medicine_editText_dosage_layout.getEditText().getText().toString());
                    prm.setMedicineID(hiddenMedicineValue.getText().toString());
                    prm.setMedicineTypeID(hiddenMedicineTypeValue.getText().toString());
                } else {
                    item_add_medicine_editText_dosage_layout.setError(getString(R.string.error_field_empty));
                    checkValid++;
                }
                //Check instruction empty
                if (medicine_confirm_instruction_edit_layout.getEditText().getText().toString().equals("")) {
                    medicine_confirm_instruction_edit_layout.setError(getString(R.string.error_field_empty));
                    checkValid++;
                } else {
                    prm.setInstruction(medicine_confirm_instruction_edit_layout.getEditText().getText().toString());
                }
                //Add all prediction medicine to list
                predictionMedicineList.add(prm);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "checkMedicineLayout()");
        }
        return checkValid;
    }

    /**
     * Remove view in layout add_medicine
     *
     * @param view view
     */
    private void removeView(View view) {
        medicine_confirm_layout_add_list.removeView(view);
    }

    /**
     * Add new prediction Medicine to firebase
     *
     * @param predictionMedicine predictionMedicine
     */
    private void addPredictionMedicine(PredictionMedicine predictionMedicine) {
        try {
            DatabaseReference newRef = FirebaseDatabase.getInstance()
                    .getReference(FirebaseConstants.FIREBASE_TABLE_PREDICTION_MEDICINE);
            newRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    newRef.child(mRef.push().getKey())
                            .setValue(predictionMedicine);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "addPredictionMedicine()");
        }
    }

    /**
     * Remove all predictionMedicine by predictionID
     *
     * @param predictionID prediction ID
     */
    private void removeAllPredictionMedicine(String predictionID) {
        try {
            //Find all prediction medicine where predictionID = "value"
            Query QGetMedicine = FirebaseDatabase.getInstance()
                    .getReference(FirebaseConstants.FIREBASE_TABLE_PREDICTION_MEDICINE)
                    .orderByChild("predictionID").equalTo(predictionID);
            QGetMedicine.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    for (DataSnapshot sn : snapshot.getChildren()) {
                        sn.getRef().removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "removeAllPredictionMedicine()");
        }
    }

    /**
     * Create a {@link TextWatcher} to clear {@link TextInputLayout} error notification
     *
     * @return a {@link TextWatcher}
     */
    private TextWatcher clearErrorOnTyping(TextInputLayout layout) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    layout.setError(null);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(LOG_TAG, "clearErrorOnTyping()");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
    }

}