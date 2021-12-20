package com.example.diseaseprediction;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.diseaseprediction.firebase.FirebaseConstants;
import com.example.diseaseprediction.listener.NetworkChangeListener;
import com.example.diseaseprediction.object.Account;
import com.example.diseaseprediction.object.DoctorInfo;
import com.example.diseaseprediction.object.Specialization;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class AccountEdit extends AppCompatActivity {
    private static final String TAG = "AccountEdit";
    private final int PICK_IMAGE_REQUEST = 71;
    private boolean isModified = false;
    //Internet connection
    private NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    //Firebase
    private DatabaseReference mRef;
    private FirebaseUser fUser;
    private Account mAccount;
    private DoctorInfo mDoctor;
    private Specialization ds;
    private ArrayAdapter<Specialization> specializationAdapter;
    private ArrayList<Specialization> specialization;
    private ArrayAdapter genderAdapter;
    private TextInputLayout account_edit_txt_title_name, account_edit_txt_title_gender, account_edit_txt_title_phone,
            account_edit_txt_title_email, account_edit_txt_title_address,
            account_doctor_txt_title_experience, account_doctor_txt_title_description,
            account_doctor_txt_title_specialization;
    private AutoCompleteTextView account_spinner_gender, account_doctor_spinner_specialization;
    private ImageView chat_toolbar_img_pre;
    private LinearLayout account_layout_doctor;
    private Button account_btn_edit_done;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_edit);
        //Get current user
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        //Find view by id
        findViews();
        // Set enable of text views
        enableEdit();
        //get data and load it to UI
        getDataForUI();
        //If edit done
        account_btn_edit_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkEmpty()) {
                    dialogConfirm(0);
                }
            }
        });

        chat_toolbar_img_pre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

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
     * Display confirm dialog on back pressed
     */
    @Override
    public void onBackPressed() {
        if (isModified) {
            dialogConfirm(1);
        } else {
            super.onBackPressed();
            finish(); //clear this activity from back stack
        }

    }

    /**
     * Find view by ID
     */
    private void findViews() {
        try {
            // Back image
            chat_toolbar_img_pre = findViewById(R.id.chat_toolbar_img_pre);

            // Doctor layout
            account_layout_doctor = findViewById(R.id.account_edit_layout_doctor);

            //Button
            account_btn_edit_done = findViewById(R.id.account_edit_btn_edit_done);

            //Find view
            account_edit_txt_title_name = findViewById(R.id.account_edit_txt_title_name);

            account_edit_txt_title_gender = findViewById(R.id.account_edit_txt_title_gender);

            account_edit_txt_title_phone = findViewById(R.id.account_edit_txt_title_phone);

            account_edit_txt_title_email = findViewById(R.id.account_edit_txt_title_email);

            account_edit_txt_title_address = findViewById(R.id.account_edit_txt_title_address);

            account_doctor_txt_title_experience = findViewById(R.id.account_edit_doctor_txt_title_experience);

            account_doctor_txt_title_description = findViewById(R.id.account_edit_doctor_txt_title_description);

            account_doctor_txt_title_specialization = findViewById(R.id.account_edit_doctor_txt_title_specialization);

            account_doctor_spinner_specialization = findViewById(R.id.account_edit_doctor_spinner_specialization);

            //Set event for layout
            account_edit_txt_title_name.getEditText().addTextChangedListener(clearErrorOnTyping(account_edit_txt_title_name));
            account_edit_txt_title_gender.getEditText().addTextChangedListener(clearErrorOnTyping(account_edit_txt_title_gender));
            account_edit_txt_title_phone.getEditText().addTextChangedListener(clearErrorOnTyping(account_edit_txt_title_phone));
            account_edit_txt_title_email.getEditText().addTextChangedListener(clearErrorOnTyping(account_edit_txt_title_email));
            account_edit_txt_title_address.getEditText().addTextChangedListener(clearErrorOnTyping(account_edit_txt_title_address));

            account_doctor_txt_title_experience.getEditText().addTextChangedListener(clearErrorOnTyping(account_doctor_txt_title_experience));
            account_doctor_txt_title_description.getEditText().addTextChangedListener(clearErrorOnTyping(account_doctor_txt_title_description));
            account_doctor_txt_title_specialization.getEditText().addTextChangedListener(clearErrorOnTyping(account_doctor_txt_title_specialization));

            //Spinner
            account_spinner_gender = findViewById(R.id.account_edit_spinner_gender);
            //Set data for spinner
            ArrayList<String> gender = new ArrayList<String>();
            gender.add(getString(R.string.default_gender_male));
            gender.add(getString(R.string.default_gender_female));
            genderAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, gender);
            account_spinner_gender.setAdapter(genderAdapter);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "findViews()");
        }
    }

    /**
     * Check empty edit text and spinner
     *
     * @return true if all inputs valid
     */
    private boolean checkEmpty() {
        boolean isValid = true;
        try {
            if (account_edit_txt_title_name.getEditText().getText().toString().trim().isEmpty()) {
                account_edit_txt_title_name.setError(getString(R.string.default_empty_name));
                isValid = false;
            }
            if (account_spinner_gender.getText().toString().trim().isEmpty()) {
                account_spinner_gender.setError(getString(R.string.default_empty_gender));
                isValid = false;
            }
            if (account_edit_txt_title_phone.getEditText().getText().toString().trim().isEmpty()) {
                account_edit_txt_title_phone.setError(getString(R.string.default_empty_phone));
                isValid = false;
            } else if (account_edit_txt_title_phone.getEditText().isEnabled()) { // if phone edit text is enable => check
                // phone number
                String phone = account_edit_txt_title_phone.getEditText().getText().toString().trim();
                if ((phone.startsWith("0") && phone.length() != 10) || // phone length must equal to 10 when
                        // start with '0'
                        (!phone.startsWith("0") && phone.length() != 9)) { // phone number length must equal to 9 when
                    // not start with '0'
                    account_edit_txt_title_phone.setError(getString(R.string.error_login_phone_too_long));
                    isValid = false;

                }
            }
            if (account_edit_txt_title_email.getEditText().getText().toString().trim().isEmpty()) {
                account_edit_txt_title_email.setError(getString(R.string.default_empty_email));
                isValid = false;
            } else {
                if (!account_edit_txt_title_email.getEditText().getText().toString().trim()
                        .matches(AppConstants.EMAIL_PATTERN)) {
                    account_edit_txt_title_email.setError(getString(R.string.default_email_regex));
                    isValid = false;
                }
            }
            if (account_edit_txt_title_address.getEditText().getText().toString().trim().isEmpty()) {
                account_edit_txt_title_address.setError(getString(R.string.default_empty_address));
                isValid = false;
            }
            //If account type is doctor
            if (account_layout_doctor.getVisibility() == View.VISIBLE) {
                if (account_doctor_txt_title_experience.getEditText().getText().toString().trim().isEmpty()) {
                    account_doctor_txt_title_experience.setError(getString(R.string.default_empty_experience));
                    isValid = false;
                }
                if (account_doctor_txt_title_description.getEditText().getText().toString().trim().isEmpty()) {
                    account_doctor_txt_title_description.setError(getString(R.string.default_empty_description));
                    isValid = false;
                }
                if (account_doctor_txt_title_specialization.getEditText().getText().toString().trim().isEmpty()) {
                    account_doctor_txt_title_specialization.setError(getString(R.string.default_empty_specialization));
                    isValid = false;
                }
            }

        } catch (NullPointerException e) {
            Log.e(TAG, "An EditText is null", e);
            Toast.makeText(this, getText(R.string.error_unknown_contactDev), Toast.LENGTH_LONG).show();
            isValid = false;
        }
        return isValid;
    }

    /**
     * Create a dialog for confirmation purpose
     *
     * @param type Type of dialog.
     *             <ul>
     *             <li>0: Save confirmation dialog</li>
     *             <li>1: Discard changes confirmation dialog</li>
     *             </ul>
     */
    private void dialogConfirm(int type) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if (type == 0) { // save confirm dialog
                builder.setMessage(getString(R.string.dialog_confirm_change_account));
                builder.setPositiveButton(getString(R.string.dialog_confirm_change_account_yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                updateValue();
                                // set no modified
                                isModified = false;
                                onBackPressed();
                            }
                        });
                builder.setNegativeButton(getString(R.string.dialog_confirm_change_account_no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });

            } else if (type == 1) { // discard changes dialog
                builder.setMessage(R.string.dialog_confirm_discard_changes);
                builder.setPositiveButton(getString(R.string.dialog_confirm_change_account_yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });
                builder.setNegativeButton(getString(R.string.dialog_confirm_change_account_no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });

            }
            builder.create().show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "dialogConfirm()");
        }
    }


    /**
     * Enable edit text base on account login type
     */
    private void enableEdit() {
        try {
            // disable edit text
            account_edit_txt_title_email.setEnabled(false);
            account_edit_txt_title_phone.setEnabled(false);

            mAccount = new Account();
            mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_ACCOUNT).child(fUser.getUid());
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mAccount = snapshot.getValue(Account.class);
                    // user log in with phone => can edit mail
                    if (mAccount.getTypeLogin() == 0) {
                        account_edit_txt_title_email.setEnabled(true);
                    } else {
                        account_edit_txt_title_phone.setEnabled(true);
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "enableEdit()");
        }
    }


    /**
     * Get data for UI from Firebase
     */
    private void getDataForUI() {
        try {
            //get user by id
            mAccount = new Account();
            mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_ACCOUNT);
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //check user exist in firebase
                    //if exist then set UI
                    if (snapshot.hasChild(fUser.getUid())) {
                        mAccount = snapshot.child(fUser.getUid()).getValue(Account.class);
                        try {
                            //Set Doctor information by type
                            if (mAccount.getTypeID() == 0) {
                                account_layout_doctor.setVisibility(View.VISIBLE);
                                loadDataOfDoctor();
                            } else {
                                account_layout_doctor.setVisibility(View.GONE);
                            }
                            //Set name
                            if (!mAccount.getName().equals("Default")) {
                                String name = mAccount.getName();
                                account_edit_txt_title_name.getEditText().setText(name);
                            }

                            //Set gender
                            int gender = mAccount.getGender();
                            String genderStr = account_spinner_gender.getAdapter().getItem(gender).toString();
                            account_spinner_gender.setText(genderStr);
                            genderAdapter.getFilter().filter(null);

                            //Set phone
                            if (!mAccount.getPhone().equals("Default")) {
                                String phone = mAccount.getPhone();
                                account_edit_txt_title_phone.getEditText().setText(phone);
                            }

                            //Set email
                            if (!mAccount.getEmail().equals("Default")) {
                                String email = mAccount.getEmail();
                                account_edit_txt_title_email.getEditText().setText(email);
                            }

                            //Set address
                            if (!mAccount.getAddress().equals("Default")) {
                                String address = mAccount.getAddress();
                                account_edit_txt_title_address.getEditText().setText(address);
                            }

//                            //Set image
//                            if (!mAccount.getImage().equals("Default")) {
//                                Glide.with(AccountEdit.this).load(mAccount.getImage()).into(account_img_avatar);
//                            } else {
//                                account_img_avatar.setImageResource(R.mipmap.ic_default_avatar_round);
//                            }
                        } catch (NullPointerException e) {
                            Log.d(TAG, "getDataForUI: Account ID null", e);
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "getDataForUI()");
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
                    isModified = true;
                    layout.setError(null);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "clearErrorOnTyping()");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
    }


    /**
     * Update value to database
     * Then update all text view
     */
    private void updateValue() {
        try {
            mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_ACCOUNT);
            //Name
            if (account_edit_txt_title_name.getEditText().getText().toString().equals("")) {
                mRef.child(fUser.getUid()).child("name").setValue("Default");
            } else {
                mRef.child(fUser.getUid()).child("name").setValue(account_edit_txt_title_name.getEditText().getText().toString());
            }

            //Gender
            if (account_spinner_gender.getText().toString().equals(account_spinner_gender.getAdapter().getItem(0).toString())) {
                mRef.child(fUser.getUid()).child("gender").setValue(0);
            } else if (account_spinner_gender.getText().toString().equals(account_spinner_gender.getAdapter().getItem(1).toString())) {
                mRef.child(fUser.getUid()).child("gender").setValue(1);
            } else {
                mRef.child(fUser.getUid()).child("gender").setValue(-1);
            }

            //Phone
            if (account_edit_txt_title_phone.getEditText().getText().toString().equals("")) {
                mRef.child(fUser.getUid()).child("phone").setValue("Default");
            } else {
                mRef.child(fUser.getUid()).child("phone").setValue(account_edit_txt_title_phone.getEditText().getText().toString());
            }

            //email
            if (account_edit_txt_title_email.getEditText().getText().toString().equals("")) {
                mRef.child(fUser.getUid()).child("email").setValue("Default");
            } else {
                mRef.child(fUser.getUid()).child("email").setValue(account_edit_txt_title_email.getEditText().getText().toString());
            }

            //address
            if (account_edit_txt_title_address.getEditText().getText().toString().equals("")) {
                mRef.child(fUser.getUid()).child("address").setValue("Default");
            } else {
                mRef.child(fUser.getUid()).child("address").setValue(account_edit_txt_title_address.getEditText().getText().toString());
            }

            if (account_layout_doctor.getVisibility() == View.VISIBLE) {
                saveDataOfDoctor();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "updateValue()");
        }
    }

    /**
     * Load all data of current account
     */
    private void loadDataOfDoctor() {
        try {
            //get user by id
            mDoctor = new DoctorInfo();
            mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_DOCTOR_INFO);
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //check user exist in firebase
                    //if exist then set UI
                    System.out.println(fUser.getUid());
                    if (snapshot.hasChild(fUser.getUid())) {
                        mDoctor = snapshot.child(fUser.getUid()).getValue(DoctorInfo.class);
                        try {
                            //Set spinner
                            specialization = new ArrayList<Specialization>();
                            mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_SPECIALIZATION);
                            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot sh : snapshot.getChildren()) {
                                        ds = sh.getValue(Specialization.class);
                                        try {
                                            assert ds != null;
                                            if (mDoctor.getSpecializationID().equals(ds.getSpecializationID())) {
                                                String specialization = ds.getName();
                                                account_doctor_spinner_specialization.setText(specialization);
                                            }
                                            specialization.add(ds);
                                        } catch (NullPointerException e) {
                                            Log.d(TAG, "Account. loadDataOfDoctor", e);
                                        }
                                    }
                                    //Set spinner
                                    specializationAdapter = new ArrayAdapter<Specialization>(AccountEdit.this,
                                            R.layout.support_simple_spinner_dropdown_item, specialization);
                                    account_doctor_spinner_specialization.setAdapter(specializationAdapter);
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                }

                            });


                            if (mDoctor.getExperience() != -1) {
                                String experience = String.valueOf(mDoctor.getExperience());
                                account_doctor_txt_title_experience.getEditText().setText(experience);
                            }

                            if (!mDoctor.getShortDescription().equals("Default")) {
                                String description = mDoctor.getShortDescription();
                                account_doctor_txt_title_description.getEditText().setText(description);
                            }
                        } catch (NullPointerException e) {
                            Log.d(TAG, "Account. loadDataOfDoctor", e);
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "loadDataOfDoctor()");
        }
    }

    /**
     * Save data to firebase
     */
    private void saveDataOfDoctor() {
        try {
            mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_DOCTOR_INFO);

            if (account_doctor_txt_title_experience.getEditText().getText().toString().equals("")) {
                mRef.child(fUser.getUid()).child("experience").setValue(-1);
            } else {
                mRef.child(fUser.getUid()).child("experience").setValue(Double.valueOf(account_doctor_txt_title_experience.getEditText().getText().toString()));
            }

            if (account_doctor_txt_title_description.getEditText().getText().toString().equals("")) {
                mRef.child(fUser.getUid()).child("shortDescription").setValue("Default");
            } else {
                mRef.child(fUser.getUid()).child("shortDescription").setValue(account_doctor_txt_title_description.getEditText().getText().toString());
            }

            if (account_doctor_spinner_specialization.getText().toString().equals("")) {
                mRef.child(fUser.getUid()).child("specializationID").setValue("Default");
            } else {
                mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_SPECIALIZATION);
                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot sh : snapshot.getChildren()) {
                            ds = sh.getValue(Specialization.class);
                            try {
                                assert ds != null;
                                if (ds.getName().equals(account_doctor_spinner_specialization.getText().toString())) {
                                    mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_DOCTOR_INFO);
                                    mRef.child(fUser.getUid()).child("specializationID").setValue(ds.getSpecializationID());
                                }
                            } catch (NullPointerException e) {
                                Log.d(TAG, "Account. saveDataOfDoctor", e);
                            }


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }

                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "saveDataOfDoctor()");
        }
    }


}