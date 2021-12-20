package com.example.diseaseprediction.object;

import java.util.Date;

/**
 * Advise for disease
 */
public class Advise {
    private String adviseID;
    private String description;
    private Date dateCreate;
    private Date dateUpdate;
    private int status;

    /**
     * Advise constructor
     */
    public Advise() {
    }

    /**
     * Create new advise
     *
     * @param adviseID    Advise ID
     * @param description Advise description
     * @param dateCreate  Date create advise
     * @param dateUpdate  Date update advise
     * @param status      0: Deleted | 1: Normal
     */
    public Advise(String adviseID, String description, Date dateCreate, Date dateUpdate, int status) {
        this.adviseID = adviseID;
        this.description = description;
        this.dateCreate = dateCreate;
        this.dateUpdate = dateUpdate;
        this.status = status;
    }

    /**
     * Get Advise ID
     *
     * @return Advise ID
     */
    public String getAdviseID() {
        return adviseID;
    }

    /**
     * Set Advise ID
     *
     * @param adviseID Advise ID
     */
    public void setAdviseID(String adviseID) {
        this.adviseID = adviseID;
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
     * Get Status
     *
     * @return 0: Deleted | 1: Normal
     */
    public int getStatus() {
        return status;
    }

    /**
     * Set Status. 0: Deleted | 1: Normal
     *
     * @param status 0: Deleted | 1: Normal
     */
    public void setStatus(int status) {
        this.status = status;
    }
}
