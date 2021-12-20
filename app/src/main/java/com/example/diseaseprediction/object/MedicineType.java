package com.example.diseaseprediction.object;

import java.util.Date;

public class MedicineType {

    private String medicineTypeID;
    private String medicineName;
    private Date dateCreate;
    private Date dateUpdate;
    private int status;

    public MedicineType() {

    }

    public MedicineType(String medicineTypeID, String medicineName, Date dateCreate, Date dateUpdate, int status) {
        this.medicineTypeID = medicineTypeID;
        this.medicineName = medicineName;
        this.dateCreate = dateCreate;
        this.dateUpdate = dateUpdate;
        this.status = status;
    }

    public String getMedicineTypeID() {
        return medicineTypeID;
    }

    public void setMedicineTypeID(String medicineTypeID) {
        this.medicineTypeID = medicineTypeID;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public Date getDateCreate() {
        return dateCreate;
    }

    public void setDateCreate(Date dateCreate) {
        this.dateCreate = dateCreate;
    }

    public Date getDateUpdate() {
        return dateUpdate;
    }

    public void setDateUpdate(Date dateUpdate) {
        this.dateUpdate = dateUpdate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    @Override
    public String toString() {
        return this.getMedicineName();
    }
}
