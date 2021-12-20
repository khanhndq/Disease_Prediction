package com.example.diseaseprediction.object;

/**
 * Prediction Symptom
 */
public class PredictionSymptom {
    private String predictionID;
    private String symptomID;
    private int status;

    /**
     * Constructor
     *
     * @param predictionID prediction ID
     * @param symptomID    symptom ID
     * @param status       status  0: Deleted | 1: Normal
     */
    public PredictionSymptom(String predictionID, String symptomID, int status) {
        this.predictionID = predictionID;
        this.symptomID = symptomID;
        this.status = status;
    }

    /**
     * Get prediction ID
     *
     * @return prediction ID
     */
    public String getPredictionID() {
        return predictionID;
    }

    /**
     * Set prediction ID
     *
     * @param predictionID prediction ID
     */
    public void setPredictionID(String predictionID) {
        this.predictionID = predictionID;
    }

    /**
     * Get symptom ID
     *
     * @return symptom ID
     */
    public String getSymptomID() {
        return symptomID;
    }

    /**
     * Set symptom ID
     *
     * @param symptomID symptom ID
     */
    public void setSymptomID(String symptomID) {
        this.symptomID = symptomID;
    }

    /**
     * Get status  0: Deleted | 1: Normal
     *
     * @return status
     */
    public int getStatus() {
        return status;
    }

    /**
     * Set status  0: Deleted | 1: Normal
     *
     * @param status
     */
    public void setStatus(int status) {
        this.status = status;
    }

}
