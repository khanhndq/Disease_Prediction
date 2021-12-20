package com.example.diseaseprediction.listener;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.example.diseaseprediction.R;

public class NetworkChangeListener extends BroadcastReceiver {
    /**
     * Check if the internet is connected or not
     *
     * @param context context
     * @return isConnected
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!isNetworkConnected(context)) {
            //Create alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View view = LayoutInflater.from(context).inflate(R.layout.disconnect_internet, null);
            builder.setView(view);
            builder.setCancelable(false);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            //Clear dialog
            Button btn = view.findViewById(R.id.disconnect_internet_btn_retry);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                    onReceive(context, intent);
                }
            });
        }
    }
}
