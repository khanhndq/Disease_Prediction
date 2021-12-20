package com.example.diseaseprediction.object;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * Disease
 */
public class Disease {
    private String diseaseID;
    private String specializationID;
    private String name;
    private String description;
    private Date dateCreate;
    private Date dateUpdate;
    private int status;

    /**
     * Disease constructor
     */
    public Disease() {
    }

    /**
     * Create disease
     *
     * @param diseaseID        Disease ID
     * @param specializationID specialization
     * @param name             Name of disease
     * @param description      Disease description
     * @param dateCreate       Date create disease
     * @param dateUpdate       Date update disease
     * @param status           0: Deleted | 1: Normal
     */
    public Disease(String diseaseID, String specializationID, String name, String description, Date dateCreate, Date dateUpdate, int status) {
        this.diseaseID = diseaseID;
        this.specializationID = specializationID;
        this.name = name;
        this.description = description;
        this.dateCreate = dateCreate;
        this.dateUpdate = dateUpdate;
        this.status = status;
    }

    /**
     * Get Disease ID
     *
     * @return Disease ID
     */
    public String getDiseaseID() {
        return diseaseID;
    }

    /**
     * Set Disease ID
     *
     * @param diseaseID Disease ID
     */
    public void setDiseaseID(String diseaseID) {
        this.diseaseID = diseaseID;
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
     * Get Name
     *
     * @return Name
     */
    public String getName() {
        return name;
    }

    /**
     * Set Name
     *
     * @param name Name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get Description
     *
     * @return Description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set Description
     *
     * @param description Description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get Date create
     *
     * @return Date create
     */
    public Date getDateCreate() {
        return dateCreate;
    }

    /**
     * Set Date create
     *
     * @param dateCreate Date create
     */
    public void setDateCreate(Date dateCreate) {
        this.dateCreate = dateCreate;
    }

    /**
     * Get Date update
     *
     * @return Date update
     */
    public Date getDateUpdate() {
        return dateUpdate;
    }

    /**
     * Set Date update
     *
     * @param dateUpdate Date update
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

    @NonNull
    @NotNull
    @Override
    public String toString() {
        return this.getName();
    }
}
