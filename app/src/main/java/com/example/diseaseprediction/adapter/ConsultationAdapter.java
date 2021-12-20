package com.example.diseaseprediction.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.diseaseprediction.Chat;
import com.example.diseaseprediction.R;
import com.example.diseaseprediction.firebase.FirebaseConstants;
import com.example.diseaseprediction.object.Message;
import com.example.diseaseprediction.object.Session;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConsultationAdapter extends RecyclerView.Adapter<ConsultationAdapter.ViewHolder> {
    private DatabaseReference mRef;
    private FirebaseUser fUser;

    private static final String LOG_TAG = "Consultation adapter";
    private Context mContext;
    private String latestMessage, latestTime;
    private List<Session> mConsultationList;
    private SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");

    public ConsultationAdapter(Context context, List<Session> mConsultationList, int sizeLoad) {
        try {
            //Get current user
            fUser = FirebaseAuth.getInstance().getCurrentUser();

            this.mContext = context;

            //Limit row load
            this.mConsultationList = new ArrayList<>();
            if (sizeLoad >= mConsultationList.size()) {
                this.mConsultationList = mConsultationList;
            } else {
                for (int i = 0; i < sizeLoad; i++) {
                    this.mConsultationList.add(mConsultationList.get(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "ConsultationAdapter()");
        }
    }

    //
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_consultation, parent, false);
        return new ConsultationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ConsultationAdapter.ViewHolder holder, int position) {
        try {
            //Collections.reverse(mConsultationList);
            Session consultation = mConsultationList.get(position);
            //Get username depend on current account
            if (consultation.getAccountIDOne().equals(fUser.getUid())) {
                getUserName(consultation.getAccountIDTwo(), holder.item_consultation_txt_name, holder.item_consultation_img_main);
            } else {
                getUserName(consultation.getAccountIDOne(), holder.item_consultation_txt_name, holder.item_consultation_img_main);
            }
            //Get messeage
            getLatestMessageAndTime(consultation.getSessionID(), holder.item_consultation_txt_message, holder.item_consultation_txt_time);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(mContext, Chat.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //Open chat depend on current account
                    if (consultation.getAccountIDOne().equals(fUser.getUid())) {
                        i.putExtra("receiverID", consultation.getAccountIDTwo());
                    } else {
                        i.putExtra("receiverID", consultation.getAccountIDOne());
                    }
                    i.putExtra("sessionID", consultation.getSessionID());
                    i.putExtra("isChatBot", false);
                    mContext.startActivity(i);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "onBindViewHolder()");
        }
    }

    @Override
    public int getItemCount() {
        return mConsultationList.size();
    }

    //Get user by user ID
    public void getUserName(String userId, TextView item_consultation_txt_name, CircleImageView item_consultation_img_main) {
        try {
            mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_ACCOUNT).child(userId);
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    item_consultation_txt_name.setText(snapshot.child("name").getValue().toString());
                    Glide.with(mContext)
                        .load(snapshot.child("image").getValue().toString())
                        .error(R.mipmap.ic_default_avatar_round)
                        .into(item_consultation_img_main);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            // Set BOLD
            item_consultation_txt_name.setTypeface(null, Typeface.BOLD);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "getUserName()");
        }
    }

    //Get message by session ID
    public void getLatestMessageAndTime(String sessionID, TextView item_consultation_txt_message, TextView item_consultation_txt_time) {
        try {
            Message latestMessage = new Message();
            mRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.FIREBASE_TABLE_MESSAGE + "/" + sessionID);
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot sn : snapshot.getChildren()) {
                        Message msg = sn.getValue(Message.class);
                        try {
                            if (msg.getSessionID().equals(sessionID)) {
                                latestMessage.setMessage(msg.getMessage());
                                latestMessage.setSenderID(msg.getSenderID());
                                latestTime = sdf.format(msg.getDateSend().getTime());
                            }

                        } catch (NullPointerException e) {
                            Log.d(LOG_TAG, "Consultation. Session ID null", e);
                        }
                    }
                    try {
                        // if current user is sender => add 'You: ' before message
                        if (latestMessage.getSenderID().equals(fUser.getUid())) {
                            item_consultation_txt_message.setText(mContext.getString(R.string.consultation_preview_message,
                                    latestMessage.getMessage()));
                        } else {
                            item_consultation_txt_message.setText(latestMessage.getMessage());
                        }

                        item_consultation_txt_time.setText(latestTime);
                    } catch (NullPointerException e) {
                        Log.d(LOG_TAG, "Consultation. Session ID null", e);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "getLatestMessageAndTime()");
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView item_consultation_txt_name, item_consultation_txt_message, item_consultation_txt_time;
        public CircleImageView item_consultation_img_main;

        public ViewHolder(View view) {
            super(view);
            item_consultation_txt_name = view.findViewById(R.id.item_consultation_txt_name);
            item_consultation_txt_message = view.findViewById(R.id.item_consultation_txt_message);
            item_consultation_txt_time = view.findViewById(R.id.item_consultation_txt_time);
            item_consultation_img_main = view.findViewById(R.id.item_consultation_img_main);
        }
    }
}
