package com.example.diseaseprediction.object;

/**
 * Advise of disease
 */
public class DiseaseAdvise {
    private String diseaseID;
    private String adviseID;
    private int status;

    /**
     * Constructor
     */
    public DiseaseAdvise() {
    }

    /**
     * Create advise of disease
     *
     * @param diseaseID disease ID
     * @param adviseID  advise ID
     * @param status    0: Deleted | 1: Normal
     */
    public DiseaseAdvise(String diseaseID, String adviseID, int status) {
        this.diseaseID = diseaseID;
        this.adviseID = adviseID;
        this.status = status;
    }

    /**
     * Get disease ID
     *
     * @return disease ID
     */
    public String getDiseaseID() {
        return diseaseID;
    }

    /**
     * Set disease ID
     *
     * @param diseaseID disease ID
     */
    public void setDiseaseID(String diseaseID) {
        this.diseaseID = diseaseID;
    }

    /**
     * Get advise ID
     *
     * @return advise ID
     */
    public String getAdviseID() {
        return adviseID;
    }

    /**
     * Set advise ID
     *
     * @param adviseID advise ID
     */
    public void setAdviseID(String adviseID) {
        this.adviseID = adviseID;
    }

    /**
     * Get status 0: Deleted | 1: Normal
     *
     * @return status
     */
    public int getStatus() {
        return status;
    }

    /**
     * Set status 0: Deleted | 1: Normal
     *
     * @param status status
     */
    public void setStatus(int status) {
        this.status = status;
    }
}
