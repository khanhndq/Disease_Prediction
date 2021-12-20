package com.example.diseaseprediction.object;

import java.util.Date;

/**
 * Account
 */
public class Account {
    private String accountId;
    private int typeID;
    private int typeLogin;
    private String phone;
    private String name;
    private int gender;
    private String address;
    private String email;
    private String image;
    private Date dateCreate;
    private Date dateUpdate;
    private int status;

    public Account() {
    }

    /**
     * Create new Account
     *
     * @param accountId  Account ID
     * @param typeID     0: Doctor | 1: User
     * @param typeLogin  0: Phone | 1: Google
     * @param phone      Phone number
     * @param name       User name
     * @param gender     Gender
     * @param address    User address
     * @param email      User Email
     * @param image      User avatar
     * @param dateCreate Account created date
     * @param dateUpdate Account update date
     * @param status     0: Deleted | 1: Normal
     */
    public Account(String accountId, int typeID, int typeLogin, String phone, String name, int gender, String address, String email,
                   String image, Date dateCreate, Date dateUpdate, int status) {
        this.accountId = accountId;
        this.typeID = typeID;
        this.typeLogin = typeLogin;
        this.phone = phone;
        this.name = name;
        this.gender = gender;
        this.address = address;
        this.email = email;
        this.image = image;
        this.dateCreate = dateCreate;
        this.dateUpdate = dateUpdate;
        this.status = status;
    }

    /**
     * Get Account ID
     *
     * @return account ID
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * Set account ID
     *
     * @param accountId account ID
     */
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    /**
     * Get Account Type
     *
     * @return type number
     */
    public int getTypeID() {
        return typeID;
    }

    /**
     * Set Account Type
     *
     * @param typeID type number
     */
    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    /**
     * Get type login
     *
     * @return type login
     */
    public int getTypeLogin() {
        return typeLogin;
    }

    /**
     * Set type login
     *
     * @param typeLogin type login
     */
    public void setTypeLogin(int typeLogin) {
        this.typeLogin = typeLogin;
    }

    /**
     * Get Phone number
     *
     * @return phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Set phone number
     *
     * @param phone phone number
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Get user name
     *
     * @return user name
     */
    public String getName() {
        return name;
    }

    /**
     * Set user name
     *
     * @param name user name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get user gender
     *
     * @return user gender
     */
    public int getGender() {
        return gender;
    }

    /**
     * Set user gender
     *
     * @param gender user gender
     */
    public void setGender(int gender) {
        this.gender = gender;
    }

    /**
     * Get address
     *
     * @return Address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Set address
     *
     * @param address address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Get Email
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set Email
     *
     * @param email email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get avatar image path
     *
     * @return avatar image path
     */
    public String getImage() {
        return image;
    }

    /**
     * Set avatar image path
     *
     * @param image avatar image path
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Get account created date
     *
     * @return account create date
     */
    public Date getDateCreate() {
        return dateCreate;
    }

    /**
     * Set account created date
     *
     * @param dateCreate account created date
     */
    public void setDateCreate(Date dateCreate) {
        this.dateCreate = dateCreate;
    }

    /**
     * Get account updated date
     *
     * @return account updated date
     */
    public Date getDateUpdate() {
        return dateUpdate;
    }

    /**
     * Set account updated date
     *
     * @param dateUpdate account updated date
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
