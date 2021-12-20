package com.example.diseaseprediction.listener;

import android.view.View;

public interface PredictClickListener {
    /**
     * click button predict on chat box
     * @param button
     * @param position
     */
    void onPredict(View button, int position);
}
