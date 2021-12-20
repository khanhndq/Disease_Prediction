package com.example.diseaseprediction.object;

/**
 * Prediction Medicine
 */
public class PredictionMedicine {

    private String predictionID;
    private String medicineID;
    private String dosage;
    private String medicineTypeID;
    private String notes;
    private String instruction;
    private int status;

    /**
     * Create new Prediction Medicine object
     * @param predictionID
     * @param medicineID
     * @param dosage
     * @param medicineTypeID
     * @param notes
     * @param instruction
     * @param status
     */
    public PredictionMedicine(String predictionID, String medicineID, String dosage, String medicineTypeID, String notes, String instruction, int status) {
        this.predictionID = predictionID;
        this.medicineID = medicineID;
        this.dosage = dosage;
        this.medicineTypeID = medicineTypeID;
        this.notes = notes;
        this.instruction = instruction;
        this.status = status;
    }

    /**
     * get Medicine Type ID
     *
     * @return
     */
    public String getMedicineTypeID() {
        return medicineTypeID;
    }

    /**
     * set Medicine Type ID
     *
     * @param medicineTypeID
     */
    public void setMedicineTypeID(String medicineTypeID) {
        this.medicineTypeID = medicineTypeID;
    }

    /**
     * get Instruction
     *
     * @return
     */
    public String getInstruction() {
        return instruction;
    }

    /**
     * set Instruction
     *
     * @param intruction
     */
    public void setInstruction(String intruction) {
        this.instruction = intruction;
    }

    /**
     * Constructor
     */
    public PredictionMedicine() {

    }

    /**
     * Get Prediction ID
     *
     * @return prediction ID
     */
    public String getPredictionID() {
        return predictionID;
    }

    /**
     * Set Prediction ID
     *
     * @param predictionID prediction ID
     */
    public void setPredictionID(String predictionID) {
        this.predictionID = predictionID;
    }

    /**
     * Get Medicine ID
     *
     * @return medicine ID
     */
    public String getMedicineID() {
        return medicineID;
    }

    /**
     * Set medicine ID
     *
     * @param medicineID medicine ID
     */
    public void setMedicineID(String medicineID) {
        this.medicineID = medicineID;
    }

    /**
     * Get dosage
     *
     * @return dosage
     */
    public String getDosage() {
        return dosage;
    }

    /**
     * Set dosage
     *
     * @param dosage dosage
     */
    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    /**
     * Get notes
     *
     * @return notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Set notes
     *
     * @param notes notes
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Get status. <ul>
     * <li>0: deleted</li>
     * <li>1: normal</li>
     * </ul>
     *
     * @return status
     */
    public int getStatus() {
        return status;
    }

    /**
     * Set status. <ul>
     * <li>0: deleted</li>
     * <li>1: normal</li>
     * </ul>
     *
     * @param status
     */
    public void setStatus(int status) {
        this.status = status;
    }
}
