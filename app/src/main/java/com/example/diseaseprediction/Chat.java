package com.example.diseaseprediction;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrawalsuneet.dotsloader.loaders.CircularDotsLoader;
import com.bumptech.glide.Glide;
import com.example.diseaseprediction.adapter.ChatAdapter;
import com.example.diseaseprediction.firebase.FirebaseConstants;
import com.example.diseaseprediction.listener.NetworkChangeListener;
import com.example.diseaseprediction.listener.PredictClickListener;
import com.example.diseaseprediction.model.DiseaseClassificationClient;
import com.example.diseaseprediction.model.Result;
import com.example.diseaseprediction.object.Account;
import com.example.diseaseprediction.object.Disease;
import com.example.diseaseprediction.object.Message;
import com.example.diseaseprediction.object.Prediction;
import com.example.diseaseprediction.object.PredictionMedicine;
import com.example.diseaseprediction.object.PredictionSymptom;
import com.example.diseaseprediction.object.Session;
import com.example.diseaseprediction.object.Symptom;
import com.example.diseaseprediction.object.SymptomMedicine;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class Chat extends AppCompatActivity {
    private static final String LOG_TAG = "Chat Activity";
    private static final int REQUEST_CODE_SPEECH = 10;

    //Internet connection
    private NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    private DatabaseReference mRef;
    private DatabaseReference mRef2;
    private DatabaseReference mRef3;
    private FirebaseUser fUser;
    private PopupMenu pm;

    private ChatAdapter chatAdapter;
    private Session mSession;
    private List<Message> mMessage;
    private List<Symptom> mSymptom;
    private String receiverID, sessionID;
    private RelativeLayout chat_send_message_layout;
    private RecyclerView chat_recycler_view;
    private TextView chat_toolbar_txt_name;
    private ImageView chat_toolbar_img_pre, chat_img_send, chat_toolbar_img_hamburger, chat_img_mic;
    private CircleImageView chat_toolbar_img_avatar;
    private EditText chat_txt_enter_mess;
    private String allMess = "";
    private DiseaseClassificationClient client;
    private Handler handler;
    private boolean checkClickPredict = false;
    private boolean checkStartMessage = true;
    private CircularDotsLoader circularDot;
    private TextView loadingText;
    private List<Symptom> tempSymptom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        client = new DiseaseClassificationClient(getApplicationContext());
        handler = new Handler();

        //Find view
        getViews();
        // Initialize
        fUser = FirebaseAuth.getInstance().getCurrentUser();


        //Set toolbar chat
        setToolbarChat();

        //Set UI by role
        setUIByAccountType();

        // get symptom
        getSymptomFirebase();

        //Set recycler view
        chat_recycler_view.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        chat_recycler_view.setLayoutManager(linearLayoutManager);

        //Get receiver id
        Intent intent = getIntent();
        receiverID = intent.getStringExtra("receiverID");
        sessionID = intent.getStringExtra("sessionID");

        //Check receiver and session
        //Then load all message
        if (receiverID != null && sessionID != null) {
            getUserChatData();
            //Set icon mic visibility when empty edit text
            chat_txt_enter_mess.addTextChangedListener(updateIconOnWriteWatcher());

            //send message
            chat_img_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String msg = chat_txt_enter_mess.getText().toString();
                    try {
                        if (!receiverID.equals(AppConstants.CHATBOT_ID)) {
                            chatWithDoctor(msg.trim());
                        } else {
                            chatWithChatbot(msg.trim());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(LOG_TAG, "Exception when talking with chatbot");
                    }
                }
            });

            //Input Voice
            chat_img_mic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSpeechInput();
                }
            });

            //Check is current session is end or not
            checkSessionStatus();
        } else {
            Toast toast = Toast.makeText(this, R.string.exception_chat_load_user, Toast.LENGTH_SHORT);
            toast.show();
        }


    }

    /**
     * Hide keyboard
     *
     * @param activity Current activity
     */
    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                    Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager.isAcceptingText()) {
                inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(),
                    0
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "hideSoftKeyboard()");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiverID.equals(AppConstants.CHATBOT_ID)) {
            endSession(sessionID);
        }
    }

    @Override
    public void onBackPressed() {
        if (receiverID.equals(AppConstants.CHATBOT_ID)) {
            System.out.println("session la" + sessionID);
            mRef =
                FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_SESSION).child(sessionID);
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Session ss = snapshot.getValue(Session.class);
                    // status != 0 -> ask user
                    if (ss.getStatus() != 0) {
                        dialogConfirm(sessionID);
                    } else {
                        Chat.super.onBackPressed();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Get all views in layout
     */
    private void getViews() {
        try {
            chat_txt_enter_mess = findViewById(R.id.chat_txt_enter_mess);
            chat_img_send = findViewById(R.id.chat_img_send);
            chat_img_mic = findViewById(R.id.chat_img_mic);
            chat_recycler_view = findViewById(R.id.chat_recycler_view);
            circularDot = findViewById(R.id.circularDot);
            loadingText = findViewById(R.id.loadingText);
            loadingText.setText(getString(R.string.chat_in_process_prediction));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "getViews()");
        }
    }

    /**
     * Handle when user click on "Get Prediction" button.
     * Chatbot will send some messages and create new prediction
     *
     * @param msg message
     */
    private void chatbotCreatePrediction(String msg) {
        if (!msg.equals("")) {
            // User request
            //user chat vs user
            if (receiverID.equals(AppConstants.CHATBOT_ID)) {
                try {
                    // user chat
                    Message message = new Message("", fUser.getUid()
                        , getString(R.string.chatbox_button_predict), new Date(), sessionID, 1);
                    setMessageFirebase(message);
                    circularDot.setVisibility(View.VISIBLE);
                    loadingText.setVisibility(View.VISIBLE);
                    //chatbot chat
                    //segment tokenize string
                    String token = client.tokenize(msg);
                    List<Result> results = new ArrayList<>();
                    //string token to list token
                    List<String> tokenList = Arrays.asList(token.split(" "));
                    List<String> outputSymtom = searchSymptoms(tokenList);
                    //outputSymptom index 1 is symptoms not in firebase
                    float percent = Float.valueOf(outputSymtom.get(1).length()) / Float.valueOf(token.length());
                    //not symptom >= 50%
                    if (percent < 0.5) {
                        //outputSymptom index 0 is symptoms have been filtered
                        results = client.classify(outputSymtom.get(0));
                    }
                    System.out.println("check result" + outputSymtom.get(0));
                    System.out.println(token);
                    System.out.println(outputSymtom.get(1));
                    System.out.println(Float.valueOf(outputSymtom.get(1).length()));
                    System.out.println(Float.valueOf(token.length()));
                    //get symptom user input
                    // If symptom found
                    // outputSymptom index 2 is result of find symptoms in firebase
                    if (!outputSymtom.get(2).isEmpty() && !results.isEmpty()) {
                        //print symptom list
                        Message mess = new Message("", AppConstants.CHATBOT_ID,
                            outputSymtom.get(2), new Date(), sessionID, 1);
                        setMessageFirebase(mess);

                        //get disease from model
                        String temp = getString(R.string.chat_chatbot_disease_list);
                        for (int i = 0; i <= 3; i++) {
                            // If the percentage greater than 1%
                            if (results.get(i).getConfidence() > 0.01) {
                                // If this is the last disease -> do not create new line
                                if (i == 3) {
                                    temp += String.format(getString(R.string.chat_chatbot_disease_list_item),
                                        results.get(i).getTitle(), results.get(i).getConfidence() * 100);
                                } else {
                                    temp += String.format(getString(R.string.chat_chatbot_disease_list_item),
                                        results.get(i).getTitle(), results.get(i).getConfidence() * 100) + "\n";
                                }
                            }
                        }
                        //print disease list
                        Message diseaseList = new Message("", AppConstants.CHATBOT_ID,
                            temp.trim(), new Date(),
                            sessionID, 1);
                        setMessageFirebase(diseaseList);
                        //print ""benh cua ban la"
                        Message message1 = new Message("", AppConstants.CHATBOT_ID,
                            String.format(getString(R.string.chat_chatbot_disease), results.get(0).getTitle()),
                            new Date(), sessionID, 1);
                        setMessageFirebase(message1);
                        chat_txt_enter_mess.setText("");
                        //print disease
//                        Message message2 = new Message("", AppConstants.CHATBOT_ID,
//                            , new Date(), sessionID, 1);
//                        setMessageFirebase(message2);
                        getDiseaseByNameAndCreatePredictionFirebase(results.get(0).getTitle(), fUser.getUid());
                        chat_txt_enter_mess.setText("");
                        circularDot.setVisibility(View.GONE);
                        loadingText.setVisibility(View.GONE);
                    } else { // No symptom found
                        Message mess = new Message("", AppConstants.CHATBOT_ID,
                            getString(R.string.chat_not_enough_symptom), new Date(), sessionID, 1);
                        setMessageFirebase(mess);
                        chat_txt_enter_mess.setText("");
                        circularDot.setVisibility(View.GONE);
                        loadingText.setVisibility(View.GONE);
                        Message mess2 = new Message("", AppConstants.CHATBOT_ID,
                            getString(R.string.chat_not_enough_data), new Date(), sessionID, 1);
                        setMessageFirebase(mess2);
                        getDiseaseByNameAndCreatePredictionFirebase(AppConstants.DISEASE_OTHER_NAME, fUser.getUid());
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "Exception when talking with chatbot ");
                }
            }
        }
    }

    /**
     * Send chat message from user to Firebase
     *
     * @param msg message to send
     */
    public void chatWithDoctor(String msg) {
        try {
            if (!msg.equals("")) {
                //user chat vs user
                Message message = new Message("", fUser.getUid(), msg
                    , new Date(), sessionID, 1);
                setMessageFirebase(message);
                chat_txt_enter_mess.setText("");
            } else {
                chat_txt_enter_mess.setText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "chatWithDoctor()");
        }
    }

    /**
     * Get user name, avatar and chat messages of user
     */
    public void getUserChatData() {
        try {
            mRef =
                FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_ACCOUNT).child(receiverID);
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Account receiver = snapshot.getValue(Account.class);
                    if (receiver != null) {
                        chat_toolbar_txt_name.setText(receiver.getName());
                        Glide.with(getApplicationContext())
                            .load(receiver.getImage())
                            .error(R.mipmap.ic_default_avatar_round)
                            .into(chat_toolbar_img_avatar);
                        getMessagesFirebase(fUser.getUid(), receiverID);
                    } else {
                        Log.e(LOG_TAG, "Cannot get account info");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "getUserChatData()");
        }
    }

    /**
     * Create a text watcher to update chat icon on typing
     *
     * @return a {@link TextWatcher} object
     */
    private TextWatcher updateIconOnWriteWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            //Show icon mic or send depend on edit text
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    // If input empty
                    if (chat_txt_enter_mess.getText().toString().trim().isEmpty()) {
                        chat_img_send.setVisibility(View.GONE);
                        chat_img_mic.setVisibility(View.VISIBLE);
                    } else {
                        chat_img_send.setVisibility(View.VISIBLE);
                        chat_img_mic.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "updateIconOnWriteWatcher()");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
    }

    /***
     * Search symptom from firebase
     *
     * @param tokenList list of tokenized input string
     * @return a string that contain detected symptoms
     */
    private List<String> searchSymptoms(List<String> tokenList) {
        List<String> output = new ArrayList<>();
        String notSymptom = "";
        String symptom = "";
        String result = getString(R.string.chat_chatbot_symptom);
        tempSymptom = new ArrayList<>();
        //search binary symptom
        int mid = mSymptom.size() / 2;
        boolean checkNotEmpty = false;
        for (String tk : tokenList) {
            tk = tk.replace("_", " ");
            boolean check = false;
            if (mSymptom.get(mid).getName().equals(tk)) {
                result += tk + ", ";
                symptom += tk.replace(" ", "_") + " ";
                tempSymptom.add(mSymptom.get(mid));
                checkNotEmpty = true;
            } else {

                for (int i = 0; i < mid; i++) {
                    if (mSymptom.get(i).getName().equals(tk)) {
                        result += tk + ", ";
                        check = true;
                        symptom += tk.replace(" ", "_") + " ";
                        tempSymptom.add(mSymptom.get(i));
                        checkNotEmpty = true;
                    }
                }
                if (!check) {
                    for (int i = mid + 1; i < mSymptom.size(); i++) {
                        if (mSymptom.get(i).getName().equals(tk)) {
                            result += tk + ", ";
                            tempSymptom.add(mSymptom.get(i));
                            symptom += tk.replace(" ", "_") + " ";
                            checkNotEmpty = true;
                            check = true;
                        }
                    }
                }
                if (!check) {
                    notSymptom += tk + " ";
                }
            }
        }
        if (checkNotEmpty == false)
            result = "";
        else
            //print symptom user input
            result = result.substring(0, result.length() - 2);
        output.add(symptom);
        output.add(notSymptom);
        output.add(result);
        return output;
    }

    /**
     * Set up chat toolbar
     */
    private void setToolbarChat() {
        try {
            chat_toolbar_img_avatar = findViewById(R.id.chat_toolbar_img_avatar);
            chat_toolbar_txt_name = findViewById(R.id.chat_toolbar_txt_name);
            chat_toolbar_img_pre = findViewById(R.id.chat_toolbar_img_pre);
            chat_toolbar_img_hamburger = findViewById(R.id.chat_toolbar_img_hamburger);

            //Back button
            chat_toolbar_img_pre.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });

            //Create popup menu
            pm = new PopupMenu(Chat.this, chat_toolbar_img_hamburger);
            pm.getMenuInflater().inflate(R.menu.chat_menu, pm.getMenu());
            pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.chat_menu_end_session:
                            endSession(sessionID);
                            return true;
                    }
                    return true;
                }
            });

            //Show popup menu when clicked on img_hamburger
            chat_toolbar_img_hamburger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Set popup menu
                    pm.show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "setToolbarChat()");
        }
    }

    /**
     * Set new message to Firebase
     *
     * @param msg massage that added to firebase
     */
    private void setMessageFirebase(Message msg) {
        try {
            mRef =
                FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_MESSAGE + "/" + sessionID);
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    msg.setMessageID(mRef.push().getKey());
                    mRef.child(msg.getMessageID()).setValue(msg);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "setMessageFirebase()");
        }
    }

    /**
     * End current session and hide soft keyboard
     *
     * @param currentSession Current session
     */
    private void endSession(String currentSession) {
        try {
            mRef3 =
                FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_SESSION).child(currentSession);
            mRef3.child("status").setValue(0);
            //Hide keyboard
            hideSoftKeyboard(Chat.this);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "endSession()");
        }
    }

    /**
     * Check if the chat session has ended
     * If not, then set chat input layout VISIBLE
     */
    private void checkSessionStatus() {
        try {
            mRef =
                FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_SESSION).child(sessionID);
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        Session session = snapshot.getValue(Session.class);
                        int status = session.getStatus();
                        // Session ended
                        if (status == 0) {
                            chat_send_message_layout = findViewById(R.id.chat_send_message_layout);
                            chat_send_message_layout.setVisibility(View.GONE);
                            Toast.makeText(Chat.this, getString(R.string.chat_session_ended), Toast.LENGTH_SHORT).show();
                        } else if (status == 1) {
                            chat_send_message_layout = findViewById(R.id.chat_send_message_layout);
                            chat_send_message_layout.setVisibility(View.VISIBLE);
                            // If user chat with chatbot
                            if (receiverID.equals(AppConstants.CHATBOT_ID)) {
                                dialogGuide();
                            }
                        }
                    } catch (NullPointerException e) {
                        Log.e(LOG_TAG, "Session status null");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "checkSessionStatus()");
        }
    }

    /**
     * Get chat messages of current user with another user from Firebase and load it to recycler view
     *
     * @param currentUserID ID of active user
     * @param receiverID    ID of receiver user
     */
    private void getMessagesFirebase(String currentUserID, String receiverID) {
        try {
            mMessage = new ArrayList<>();
            mRef =
                FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_MESSAGE + "/" + sessionID);
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mMessage.clear();
                    for (DataSnapshot sn : snapshot.getChildren()) {
                        Message msg = sn.getValue(Message.class);

                        if (msg != null && msg.getSenderID() != null) {
                            mMessage.add(msg);
                            if (msg.getStatus() == 3) {
                                if (checkStartMessage) {
                                    mRef2 =
                                        FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_MESSAGE + "/" + sessionID);
                                    mRef2.child(msg.getMessageID()).child("status").setValue(4);
                                }
                                if (checkClickPredict) {
                                    mRef2 =
                                        FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_MESSAGE + "/" + sessionID);
                                    mRef2.child(msg.getMessageID()).child("status").setValue(4);
                                }
                            }
                            chatAdapter = new ChatAdapter(Chat.this, mMessage);
                            chat_recycler_view.setAdapter(chatAdapter);

                            chatAdapter.setPredictButtonListener(new PredictClickListener() {
                                @Override
                                public void onPredict(View button, int position) {
                                    chatbotCreatePrediction(allMess);
                                    mRef2 =
                                        FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_MESSAGE + "/" + sessionID);
                                    mRef2.child(msg.getMessageID()).child("status").setValue(4);
                                    checkClickPredict = true;
                                }
                            });
                        }
                    }
                    checkStartMessage = false;
                    checkClickPredict = false;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "getMessagesFirebase()");
        }
    }

    /**
     * Get diseaseID by name
     * And create prediction
     *
     * @param disease Disease Name
     * @param uId     Current user ID
     */
    private void getDiseaseByNameAndCreatePredictionFirebase(String disease, String uId) {
        try {
            mRef2 = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_DISEASE);
            Query disQuery;
            disQuery = mRef2.orderByChild("name").equalTo(disease);
            disQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    try {
                        // if disease found
                        if (snapshot.hasChildren()) {
                            for (DataSnapshot sn : snapshot.getChildren()) {
                                Disease d = sn.getValue(Disease.class);

                                Prediction pre = new Prediction("0", uId, "Default",
                                    sessionID, "Default",
                                    d.getDiseaseID(), "Default", new Date(), new Date(),
                                    d.getSpecializationID(), 0);
                                // If the status of current disease is 'deleted' => replace this disease by 'other
                                // disease'
                                if (d.getStatus() == 0) {
                                    pre.setDiseaseID(AppConstants.DISEASE_OTHER_ID);
                                    pre.setHiddenSpecializationID(AppConstants.DISEASE_OTHER_SPECIALIZATION);
                                    d.setName(AppConstants.DISEASE_OTHER_NAME);
                                }
                                // If disease name is "other disease" => set Note
                                if (d.getName().equals(AppConstants.DISEASE_OTHER_NAME)) {
                                    pre.setNotes(AppConstants.DISEASE_OTHER_NAME);
                                }
                                createPrediction(pre);
                            }
                        } else {
                            Toast.makeText(Chat.this, getString(R.string.error_unknown_contactDev),
                                Toast.LENGTH_SHORT).show();
                            Log.e(LOG_TAG, "Not found disease with that name in database");
                        }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "An error happened", e);
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "getDiseaseByNameFirebase()");
        }
    }

    /**
     * Set up element base on Account Type
     */
    private void setUIByAccountType() {
        try {
            mRef =
                FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_ACCOUNT).child(fUser.getUid());
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        String accountType = Objects.requireNonNull(snapshot.child("typeID").getValue()).toString();
                        if (accountType.equals("0")) {
                            chat_toolbar_img_hamburger.setVisibility(View.VISIBLE);
                        } else {
                            chat_toolbar_img_hamburger.setVisibility(View.GONE);
                        }
                    } catch (NullPointerException e) {
                        Log.e(LOG_TAG, "Account type null", e);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "setUIByAccountType()");
        }
    }

    /**
     * Call default Speech-to-text handler of the phone. Permission checking is included
     */
    public void getSpeechInput() {
        try {
            // Check microphone access permission
            if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                // Call Voice input
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi");
                try {
                    startActivityForResult(intent, REQUEST_CODE_SPEECH);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, getString(R.string.chat_speech_support_device), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                // ask for the permission.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_CODE_SPEECH);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "getSpeechInput()");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Receive speech-to-text result
        if (requestCode == REQUEST_CODE_SPEECH) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                chat_txt_enter_mess.setText(result.get(0));
            }
        }
    }

//    /**
//     * Create recommend symptom
//     *
//     * @param rcs RecommendSymptom
//     */
//    private void createRecommendSymptom(RecommendSymptom rcs) {
//        mRef = FirebaseDatabase.getInstance().getReference("RecommendSymptom");
//        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                mRef.child(rcs.getMessageID()).setValue(rcs);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//    }

    @Override
    protected void onStart() {
        //Check internet connected or not
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
        Log.v(LOG_TAG, "onStart");
        handler.post(
            () -> {
                client.load();
            });
    }

    @Override
    protected void onStop() {
        //Check internet connected or not
        unregisterReceiver(networkChangeListener);
        super.onStop();
        Log.v(LOG_TAG, "onStop");
        handler.post(
            () -> {
                client.unload();
            });
    }

    /**
     * Create new prediction
     * Add symptoms to prediction (if any)
     * And end session
     *
     * @param pre Prediction object
     */
    private void createPrediction(Prediction pre) {
        try {
            mRef2 = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_PREDICTION);
            mRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        pre.setPredictionID(mRef2.push().getKey());
                        mRef2.child(pre.getPredictionID()).setValue(pre, new DatabaseReference.CompletionListener() {
                            @Override
                            //If new prediction are created
                            //Then end the current session of chat and display a notification dialog
                            public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error,
                                                   @NonNull @NotNull DatabaseReference ref) {
                                if (!tempSymptom.isEmpty() || tempSymptom != null) {
                                    createPredictionSymptom(pre.getPredictionID());
                                    for (int i = 0; i < tempSymptom.size(); i++) {
                                        getSymptomMedicine(tempSymptom.get(i).getSymptomID(), pre.getPredictionID());
                                    }

                                }
                                endSession(sessionID);
                                dialogPrediction(pre);
                            }
                        });


                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(LOG_TAG, "Not found disease in database", e);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "createPrediction()");
        }
    }

    /**
     * Create new prediction symptom
     *
     * @param predictionID
     */
    private void createPredictionSymptom(String predictionID) {
        try {
            mRef2 = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_PREDICTION_SYMPTOM);
            mRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        if (!tempSymptom.isEmpty() && tempSymptom != null) {
                            List<PredictionSymptom> psList = new ArrayList<>();
                            for (Symptom s : tempSymptom) {
                                psList.add(new PredictionSymptom(predictionID, s.getSymptomID(), 1));
                            }
                            for (PredictionSymptom ps : psList) {
                                mRef2.child(mRef2.push().getKey()).setValue(ps);
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(LOG_TAG, "Not found disease in database", e);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "createPrediction()");
        }
    }


    /**
     * get symptom medicine
     */
    private void getSymptomMedicine(String sympID, String predictionID) {
        try {
            //get symptom ID by prediction ID
            mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_SYMPTOM_MEDICINE);
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    for (DataSnapshot sn : snapshot.getChildren()) {
                        SymptomMedicine sm = sn.getValue(SymptomMedicine.class);
                        if (sm.getSymptomID().equals(sympID)) {
                            addPredictionMedicine(sm.getMedicineID(), predictionID);
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "checkPredictionStatus()");
        }
    }

    /**
     * get symptom medicine
     */
    private void addPredictionMedicine(String medicineID, String predictionID) {
        try {
            //get symptom ID by prediction ID
            mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_PREDICTION_MEDICINE);
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    mRef.child(mRef.push().getKey()).setValue(new PredictionMedicine(predictionID, medicineID, "3",
                        AppConstants.MEDICINE_TYPE_DEFAULT, "Default", AppConstants.MEDICINE_INSTRUCTION_DEFAULT, 1));
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "checkPredictionStatus()");
        }
    }

    /**
     * Get symptom in firebase
     */
    private void getSymptomFirebase() {
        try {
            mSymptom = new ArrayList<>();
            mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_SYMPTOM);
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mSymptom.clear();
                    for (DataSnapshot sn : snapshot.getChildren()) {
                        Symptom msg = sn.getValue(Symptom.class);
                        mSymptom.add(msg);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "getSymptomFirebase()");
        }
    }

    /**
     * Display a dialog that show to users rules that they should follow to get the best result
     */
    private void dialogGuide() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.chat_dialog_attention_title);
            builder.setMessage(R.string.chat_dialog_attention_description);
            builder.setPositiveButton(getString(R.string.chat_dialog_attention_btn_understand),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Do nothing
                    }
                });
            builder.create().show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "dialogGuide()");
        }
    }


    /**
     * Create dialog to confirm when user click on back button
     */
    private void dialogConfirm(String sessionID) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.chat_dialog_end_session_prediction);
            builder.setPositiveButton(getString(R.string.dialog_confirm_change_account_yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        endSession(sessionID);
                        onBackPressed();
                    }
                });
            builder.setNegativeButton(getString(R.string.dialog_confirm_change_account_no),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Do nothing
                    }
                });
            builder.create().show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "dialogConfirm()");
        }
    }

    /**
     * Display a dialog after the prediction had been created.
     * The dialog will notify user about the prediction and ask whether user want to check the status of their
     * prediction. If they select "Yes", they will be redirected to {@link PredictionResult} screen, else the dialog
     * will disappear.
     *
     * @param prediction The prediction created by Model
     */
    private void dialogPrediction(Prediction prediction) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.chat_dialog_prediction_title);
            builder.setMessage(R.string.chat_dialog_prediction_msg);
            builder.setPositiveButton(getString(R.string.dialog_confirm_change_account_yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Chat.this, PredictionResult.class);
                        intent.putExtra(PredictionResult.INTENT_EXTRA_PREDICTION, prediction);
                        startActivity(intent);
                    }
                });
            builder.setNegativeButton(getString(R.string.dialog_confirm_change_account_no),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Do nothing
                    }
                });
            builder.create().show();
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "dialogPrediction: null pointer", e);
        }
    }


    /**
     * Set message from user to Firebase and send chatbot response message
     *
     * @param msg user chat
     */
    public void chatWithChatbot(String msg) {
        try {
            if (!msg.equals("")) {
                Message message = new Message("", fUser.getUid()
                    , msg.trim(), new Date(), sessionID, 1);
                setMessageFirebase(message);
                //Chatbot chat
                Message message1 = new Message("", AppConstants.CHATBOT_ID,
                    getString(R.string.chat_chatbot_continue_symptom), new Date(), sessionID, 3);
                setMessageFirebase(message1);
                chat_txt_enter_mess.setText("");
                allMess += msg + " ";
            } else {
                chat_txt_enter_mess.setText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "nextChat()");
        }

    }

}