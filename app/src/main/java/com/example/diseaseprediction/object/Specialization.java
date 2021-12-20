package com.example.diseaseprediction.object;

import java.util.Date;

/**
 * Specialization of doctor
 */
public class Specialization {
    private String specializationID;
    private String name;
    private Date dateCreate;
    private Date dateUpdate;
    private int status;

    /**
     * Doctor specialization constructor
     */
    public Specialization() {
    }

    /**
     * Create doctor specialization
     *
     * @param specializationID Specialization ID
     * @param name             Name of specialization
     * @param dateCreate       Date create
     * @param dateUpdate       Date update
     * @param status           0: Deleted | 1: Normal
     */
    public Specialization(String specializationID, String name, Date dateCreate, Date dateUpdate, int status) {
        this.specializationID = specializationID;
        this.name = name;
        this.dateCreate = dateCreate;
        this.dateUpdate = dateUpdate;
        this.status = status;
    }

    /**
     * Get specialization ID
     *
     * @return specialization ID
     */
    public String getSpecializationID() {
        return specializationID;
    }

    /**
     * Set specialization ID
     *
     * @param specializationID specialization ID
     */
    public void setSpecializationID(String specializationID) {
        this.specializationID = specializationID;
    }

    /**
     * Get name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Set name
     *
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
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
     * Set status. 0: Deleted | 1: Normal
     *
     * @param status 0: Deleted | 1: Normal
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Set string return is name
     * Return name of object in spinner
     *
     * @return name
     */
    @Override
    public String toString() {
        return name;
    }
}
