package com.example.diseaseprediction.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diseaseprediction.R;
import com.example.diseaseprediction.listener.PredictClickListener;
import com.example.diseaseprediction.object.Message;
import com.example.diseaseprediction.object.Symptom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private FirebaseUser fUser;
    private DatabaseReference mRef;
    private DatabaseReference mRef2;

    private static final String LOG_TAG = "Chat adapter";
    private Context mContext;
    private List<Message> mMessage;
    private List<Symptom> mSymptom;
    private ArrayAdapter<Symptom> symptomAdapter;
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public static final int MSG_TYPE_LEFT_BOX = 2;
    PredictClickListener listener;


    public ChatAdapter(@NonNull Context context, List<Message> mMessage) {
        try {
            this.mContext = context;
            this.mMessage = mMessage;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "ChatAdapter()");
        }
    }

    public ChatAdapter(@NonNull Context context, List<Message> mMessage, PredictClickListener listener) {
        try {
            this.mContext = context;
            this.mMessage = mMessage;
            this.listener = listener;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "ChatAdapter()");
        }
    }


    @NotNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Get view type. Right layout if sender, left layout if receiver
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_chat_right, parent, false);
            return new ChatAdapter.ViewHolder(view);
        }
        if (viewType == MSG_TYPE_LEFT_BOX) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_chat_left_box, parent, false);
            return new ChatAdapter.ViewHolder(view, listener);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_chat_left, parent, false);
            return new ChatAdapter.ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        try {
            //Get message
            Message msg = mMessage.get(position);
            holder.item_chat_show_message.setText(msg.getMessage());
            //Custom date under message
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
            String shortTimeStr = sdf.format(msg.getDateSend().getTime());
            holder.item_chat_time.setText(shortTimeStr);

            //On clicked in message then show time send
            holder.item_chat_layout_show_message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.item_chat_time.getVisibility() == View.VISIBLE) {
                        holder.item_chat_time.setVisibility(View.GONE);
                    } else {
                        holder.item_chat_time.setVisibility(View.VISIBLE);
                    }
                }
            });

            if (holder.item_chat_box_button_predict != null) {
                if (msg.getStatus() == 4)
                    holder.item_chat_box_button_predict.setEnabled(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "onBindViewHolder()");
        }
    }

    @Override
    public int getItemCount() {
        return mMessage.size();
    }

    @Override
    public int getItemViewType(int position) {
        //Get type depend on user
        //Right if sender, Left if receiver
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mMessage.get(position).getSenderID().equals(fUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else if (mMessage.get(position).getStatus() == 3 || mMessage.get(position).getStatus() == 4) {
            return MSG_TYPE_LEFT_BOX;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    //setter button predict
    public void setPredictButtonListener(PredictClickListener myClickListener) {
        this.listener = myClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView item_chat_show_message, item_chat_time;
        public LinearLayout item_chat_layout_show_message;
        public Button item_chat_box_button_predict;
        PredictClickListener myClickListener;

        //constructor use for message button predict
        public ViewHolder(@NonNull View itemView, PredictClickListener listener) {
            super(itemView);
            //Find view
            item_chat_show_message = itemView.findViewById(R.id.item_chat_show_message);
            item_chat_time = itemView.findViewById(R.id.item_chat_time);
            item_chat_layout_show_message = itemView.findViewById(R.id.item_chat_layout_show_message);
            item_chat_box_button_predict = (Button) itemView.findViewById(R.id.item_chat_box_button_predict);
            this.myClickListener = listener;
            item_chat_box_button_predict.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onPredict(v, getAdapterPosition());
                }
            });
        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            //Find view
            item_chat_show_message = itemView.findViewById(R.id.item_chat_show_message);
            item_chat_time = itemView.findViewById(R.id.item_chat_time);
            item_chat_layout_show_message = itemView.findViewById(R.id.item_chat_layout_show_message);
        }
    }
}
