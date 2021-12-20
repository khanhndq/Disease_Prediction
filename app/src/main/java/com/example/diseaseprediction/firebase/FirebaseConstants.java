package com.example.diseaseprediction.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

public class FirebaseConstants {
    public static final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
    public static final String FIREBASE_TABLE_ACCOUNT = "Accounts";
    public static final String FIREBASE_TABLE_ADVISE = "Advise";
    public static final String FIREBASE_TABLE_DISEASE = "Disease";
    public static final String FIREBASE_TABLE_DISEASE_ADVISE = "DiseaseAdvise";
    public static final String FIREBASE_TABLE_DOCTOR_INFO = "DoctorInfo";
    public static final String FIREBASE_TABLE_SPECIALIZATION = "Specialization";
    public static final String FIREBASE_TABLE_MEDICINE = "Medicine";
    public static final String FIREBASE_TABLE_MESSAGE = "Message";
    public static final String FIREBASE_TABLE_PREDICTION = "Prediction";
    public static final String FIREBASE_TABLE_PREDICTION_MEDICINE = "PredictionMedicine";
    public static final String FIREBASE_TABLE_PREDICTION_SYMPTOM = "PredictionSymptom";
    public static final String FIREBASE_TABLE_SESSION = "Session";
    public static final String FIREBASE_TABLE_SYMPTOM = "Symptom";
    public static final String FIREBASE_TABLE_SYMPTOM_MEDICINE = "SymptomMedicine";
    public static final String FIREBASE_TABLE_MEDICINE_TYPE = "MedicineType";
    public static final String STORAGE_IMG = "images";
    public static StorageReference sRef;
    public static DatabaseReference mRef;
}
