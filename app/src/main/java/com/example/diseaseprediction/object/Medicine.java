package com.example.diseaseprediction.object;

import java.util.Date;

/**
 * Medicine for disease
 */
public class Medicine {
    private String medicineID;
    private String name;
    private String description;
    private String manufacturer;
    private String content;
    private Date dateCreate;
    private Date dateUpdate;
    private int status;

    /**
     * Medicine constructor
     */
    public Medicine() {
    }

    /**
     * Create new medicine
     *
     * @param medicineID   Medicine ID
     * @param name         Name of medicine
     * @param description  Description
     * @param manufacturer Manufacturer
     * @param content      Content of medicine
     * @param dateCreate   Date create
     * @param dateUpdate   Date update
     * @param status       0: Deleted | 1: Normal
     */
    public Medicine(String medicineID, String name, String description, String manufacturer,
                    String content, Date dateCreate, Date dateUpdate, int status) {
        this.medicineID = medicineID;
        this.name = name;
        this.description = description;
        this.manufacturer = manufacturer;
        this.content = content;
        this.dateCreate = dateCreate;
        this.dateUpdate = dateUpdate;
        this.status = status;
    }

    /**
     * Get Medicine ID
     *
     * @return Medicine ID
     */
    public String getMedicineID() {
        return medicineID;
    }

    /**
     * Set Medicine ID
     *
     * @param medicineID Medicine ID
     */
    public void setMedicineID(String medicineID) {
        this.medicineID = medicineID;
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
     * Get Manufacturer
     *
     * @return Manufacturer
     */
    public String getManufacturer() {
        return manufacturer;
    }

    /**
     * Set Manufacturer
     *
     * @param manufacturer Manufacturer
     */
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    /**
     * Get Content
     *
     * @return Content
     */
    public String getContent() {
        return content;
    }

    /**
     * Get Content
     *
     * @param content Content
     */
    public void setContent(String content) {
        this.content = content;
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

    @Override
    public String toString() {
        return this.getName();
    }
}
