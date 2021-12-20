package com.example.diseaseprediction;

/**
 * Store app constants
 */
public class AppConstants {
    public static final String CHATBOT_ID = "fvIVxPMjdTTygUpRT1k8Em2ZUuy2";
    public static final String CHATBOT_NAME = "Neon";

    public static final String DISEASE_OTHER_ID = "99999";
    public static final String DISEASE_OTHER_NAME = "Bệnh khác";
    public static final String DISEASE_OTHER_SPECIALIZATION = "-Mct1vQY8dBaxrRr1eog"; // specialize ID of polyclinic

    public static final String MEDICINE_OTHER_ID = "99999";
    public static final String MEDICINE_OTHER_NAME = "Thuốc khác";
    public static final String MEDICINE_TYPE_DEFAULT = "-Mfx5qJbqjsmPco3qySC";
    public static final String MEDICINE_INSTRUCTION_DEFAULT = "Ngày uống 3 lần, mỗi lần 1 viên";

    public static final String EMAIL_PATTERN = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";

    // Model Settings
    public static final String MODEL_FILE_NAME = "model-v1.1.3-metadata.tflite";
    public static final String VOCAB_FILE_NAME = "model-v1.1.3-vocab.txt";
    public static final String LABEL_FILE_NAME = "labels.txt";

    // Settings for Home Fragment
    public static final int HOME_NUM_ITEMS_PREDICTION = 5; // Define number of prediction to be loaded to Home Fragment
    public static final int HOME_NUM_ITEMS_PENDING_PREDICTION = 5; // Define number of pending prediction to be
    // loaded to Home Fragment
    public static final int HOME_NUM_ITEMS_CONSULTATION = 5; // Define number of consultation to be loaded to Home
    // Fragment
}
