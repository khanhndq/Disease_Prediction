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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.diseaseprediction.firebase.FirebaseConstants;
import com.example.diseaseprediction.listener.NetworkChangeListener;
import com.example.diseaseprediction.object.Account;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CodeVerify extends AppCompatActivity {

    public static final String INTENT_MOBILE = "mobile";
    private static final String TAG = "CodeVerifyActivity";

    //Internet connection
    private NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    private Account mAccount;
    private DatabaseReference mRef;
    private FirebaseUser fUser;

    //fireBase authentication object
    private FirebaseAuth mAuth;

    //this is the verification id that will be sent to the user
    private String mVerificationId;

    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private String phoneNumber;
    private int type = 1; //Account type: doctor or user

    private TextInputLayout code_verify_edit_txt_code_layout;
    private ImageView code_verify_img_next;
    private ImageView code_verify_img_back;
    private TextView code_verify_re_send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_verify);

        //initializing objects
        findViews();
        mAuth = FirebaseAuth.getInstance();

        // Edit Text
        code_verify_edit_txt_code_layout.getEditText().addTextChangedListener(clearErrorOnTyping());

        //Next button
        code_verify_img_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkCodeInput()) {
                    verifyPhoneNumberWithCode(mVerificationId,
                        code_verify_edit_txt_code_layout.getEditText().getText().toString());
                }
            }
        });

        //Back button
        code_verify_img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //Resend code
        code_verify_re_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendVerificationCode(phoneNumber, mResendToken);
            }
        });

        //getting mobile number from the previous activity
        //and sending the verification code to the number
        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra(Login.INTENT_MOBILE);
        type = intent.getIntExtra("type", 1);

        // Initialize phone auth callbacks
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NotNull PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    Log.e(TAG, "Phone number format is not valid", e);
                    displayOKDialog(getString(R.string.error_txt),
                        getString(R.string.code_verify_exception_phone_invalid));
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Log.e(TAG, "The SMS quota for the project has been exceeded", e);
                    displayOKDialog(getString(R.string.error_txt),
                        getString(R.string.code_verify_exception_device_blocked));
                } else {
                    // Show a message and update the UI
                    displayOKDialog(getString(R.string.error_txt),
                        getString(R.string.code_verify_exception_verification_failed_unknown));
                }


            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent to " + phoneNumber + ":" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };

        // Begin phone verification
        startPhoneNumberVerification(phoneNumber);

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
     * Find view by ID
     */
    private void findViews() {
        try {
            code_verify_img_next = findViewById(R.id.code_verify_img_next);
            code_verify_img_back = findViewById(R.id.code_verify_img_back);
            code_verify_edit_txt_code_layout = findViewById(R.id.code_verify_edit_txt_code_layout);
            code_verify_re_send = findViewById(R.id.code_verify_re_send);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "findViews()");
        }
    }

    /**
     * Check Code input
     *
     * @return true if input valid, otherwise, false
     */
    private boolean checkCodeInput() {
        try {
            String code = code_verify_edit_txt_code_layout.getEditText().getText().toString();
            // Check input empty
            if (code.isEmpty()) {
                code_verify_edit_txt_code_layout.setError(getString(R.string.error_field_empty));
                return false;
            }
            // Verify code correct
            else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "checkCodeInput()");
            return false;
        }
    }

    /**
     * Create a {@link TextWatcher} to clear {@link TextInputLayout} error notification
     *
     * @return a {@link TextWatcher}
     */
    private TextWatcher clearErrorOnTyping() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    code_verify_edit_txt_code_layout.setError(null);
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
     * Start Phone number verification process. This will first verify the app using SafetyNet or reCAPTCHA
     * in the event that SafetyNet cannot be used. Then, Firebase will send a SMS message contain the code
     * to the {@code phoneNumber} with 60 seconds timeout
     *
     * @param phoneNumber phone number to verify
     */
    private void startPhoneNumberVerification(String phoneNumber) {
        try {
            PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                    .setPhoneNumber(phoneNumber)       // Phone number to verify
                    .setTimeout(0L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(this)                 // Activity (for callback binding)
                    .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                    .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "startPhoneNumberVerification()");
        }

    }

    /**
     * Create a new {@link PhoneAuthCredential} from {@code verificationId} and {@code code}. Then, start the sign in
     * process
     *
     * @param verificationId Verification ID from {@code onCodeSent}
     * @param code           Verification code from SMS
     */
    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signInWithPhoneAuthCredential(credential);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "verifyPhoneNumberWithCode()");
        }
    }

    /**
     * Force resend verification code if time out expired but SMS not received
     *
     * @param phoneNumber phone number in '+'[country_code][register_number] format
     * @param token       ForceResendingToken from {@code onCodeSent}
     */
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        try {
            PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                    .setPhoneNumber(phoneNumber)       // Phone number to verify
                    .setTimeout(0L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(this)                 // Activity (for callback binding)
                    .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                    .setForceResendingToken(token)     // ForceResendingToken from callbacks
                    .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
            displayOKDialog("Code Sent", "The OTP code have been sent!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "resendVerificationCode()");
        }
    }

    /**
     * Start sign in process using {@link PhoneAuthCredential}
     *
     * @param credential a {@link PhoneAuthCredential} object
     */
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        try {
            mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            mAccount = new Account();
                            mRef =
                                FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_ACCOUNT);
                            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    try {
                                        //check user exist in firebase
                                        //if exist then set UI
                                        if (snapshot.hasChild(user.getUid())) {
                                            Log.d(TAG, "User exist");
                                            mAccount = snapshot.child(user.getUid()).getValue(Account.class);
                                            if (mAccount.getName().equals("Default") || mAccount.getGender() == -1 ||
                                                mAccount.getPhone().equals("Default") || mAccount.getEmail().equals(
                                                "Default") ||
                                                mAccount.getAddress().equals("Default")) {
                                                Intent intent = new Intent(CodeVerify.this, AccountInfo.class);
                                                intent.putExtra(INTENT_MOBILE, phoneNumber);
                                                startActivity(intent);
                                            } else {
                                                Intent intent = new Intent(CodeVerify.this, MainActivity.class);
                                                startActivity(intent);
                                            }
                                        } else {
                                            createNewFirebaseAccount(user);
                                        }
                                    } catch (NullPointerException e) {
                                        Log.e(TAG, "signInWithPhoneAuthCredential: UID Null", e);
                                        Toast.makeText(CodeVerify.this, getString(R.string.error_unknown_relogin),
                                            Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                }
                            });


                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                code_verify_edit_txt_code_layout.setError(getString(R.string.code_verify_error_code_invalid));
                            }
                        }
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "signInWithPhoneAuthCredential()");
        }
    }

    /**
     * Create an {@link AlertDialog} for notify purpose only. The dialog has a title of {@code title} and a content of
     * {@code msg}. The dialog has only 1 button - OK button
     *
     * @param title title of the dialog
     * @param msg   Error Message to display
     */
    private void displayOKDialog(String title, String msg) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(CodeVerify.this);

            // Set content
            builder.setTitle(title)
                .setMessage(msg);

            // Set button
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing
                }
            });

            builder.create().show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "displayOKDialog()");
        }
    }

    /**
     * Create new account in Firebase Database and open {@link AccountInfo} activity
     *
     * @param user a verified user
     */
    private void createNewFirebaseAccount(FirebaseUser user) {
        try {
            //Set default data
            int gender = -1;
            String phone = phoneNumber;
            String address = "Default";
            String email = "Default";
            String name = "Default";
            String imgURL = "Default";
            //Create new account
            Account account = new Account(user.getUid(), type, 0, phone, name, gender, address, email, imgURL
                , new Date(), new Date(), 1);
            //Save new account to firebase
            //If write data success, start new activity
            mRef.child(user.getUid()).setValue(account, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error,
                                       @NonNull @NotNull DatabaseReference ref) {
                    //Go to account information activity
                    Intent intent = new Intent(CodeVerify.this, AccountInfo.class);
                    startActivity(intent);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "createNewFirebaseAccount()");
        }
    }
}