package com.example.diseaseprediction.object;

import java.util.Date;

/**
 * Doctor information
 */
public class DoctorInfo {
    private String accountID;
    private String specializationID;
    private String shortDescription;
    private double experience;
    private Date dateCreate;
    private Date dateUpdate;
    private int status;

    /**
     * Doctor information constructor
     */
    public DoctorInfo() {
    }

    /**
     * Create doctor information
     *
     * @param accountID        Account ID
     * @param specializationID Specialization ID
     * @param shortDescription Short description
     * @param experience       Experience
     * @param dateCreate       Date create
     * @param dateUpdate       Date update
     * @param status           0: Deleted | 1: Normal
     */
    public DoctorInfo(String accountID, String specializationID, String shortDescription, double experience, Date dateCreate, Date dateUpdate, int status) {
        this.accountID = accountID;
        this.specializationID = specializationID;
        this.shortDescription = shortDescription;
        this.experience = experience;
        this.dateCreate = dateCreate;
        this.dateUpdate = dateUpdate;
        this.status = status;
    }

    /**
     * Get Account ID
     *
     * @return Account ID
     */
    public String getAccountID() {
        return accountID;
    }

    /**
     * Set Account ID
     *
     * @param accountID Account ID
     */
    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    /**
     * Get Specialization ID
     *
     * @return Specialization ID
     */
    public String getSpecializationID() {
        return specializationID;
    }

    /**
     * Set Specialization ID
     *
     * @param specializationID Specialization ID
     */
    public void setSpecializationID(String specializationID) {
        this.specializationID = specializationID;
    }

    /**
     * Get short description
     *
     * @return Short description
     */
    public String getShortDescription() {
        return shortDescription;
    }

    /**
     * Set short description
     *
     * @param shortDescription short description
     */
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    /**
     * Get Experience
     *
     * @return Experience
     */
    public double getExperience() {
        return experience;
    }

    /**
     * Set Experience
     *
     * @param experience Experience
     */
    public void setExperience(double experience) {
        this.experience = experience;
    }

    /**
     * Get date create
     *
     * @return date create
     */
    public Date getDateCreate() {
        return dateCreate;
    }

    /**
     * Set date create
     *
     * @param dateCreate date create
     */
    public void setDateCreate(Date dateCreate) {
        this.dateCreate = dateCreate;
    }

    /**
     * Get date update
     *
     * @return date update
     */
    public Date getDateUpdate() {
        return dateUpdate;
    }

    /**
     * Set date update
     *
     * @param dateUpdate date update
     */
    public void setDateUpdate(Date dateUpdate) {
        this.dateUpdate = dateUpdate;
    }

    /**
     * Get status
     *
     * @return 0: Deleted | 1: Normal
     */
    public int getStatus() {
        return status;
    }

    /**
     * Set status. 0: Deleted | 1: Normal
     *
     * @param status 0: Deleted | 1: Normal
     */
    public void setStatus(int status) {
        this.status = status;
    }
}
