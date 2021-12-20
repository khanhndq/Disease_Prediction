package com.example.diseaseprediction.object;

import java.util.Date;

/**
 * Session
 */
public class Session {
    private String sessionID;
    private String accountIDOne;
    private String accountIDTwo;
    private Date dateCreate;
    private Date dateUpdate;
    private int status;

    /**
     * Session constructor
     */
    public Session() {
    }

    /**
     * Create new session
     *
     * @param sessionID    Session ID
     * @param dateCreate   Date create
     * @param dateUpdate   Date update
     * @param accountIDOne account ID One
     * @param accountIDTwo account ID Two
     * @param status       0: End session | 1: In session
     */
    public Session(String sessionID, String accountIDOne, String accountIDTwo, Date dateCreate, Date dateUpdate, int status) {
        this.sessionID = sessionID;
        this.accountIDOne = accountIDOne;
        this.accountIDTwo = accountIDTwo;
        this.dateCreate = dateCreate;
        this.dateUpdate = dateUpdate;
        this.status = status;
    }

    /**
     * Create new session
     *
     * @param sessionID  Session ID
     * @param dateCreate Date create
     * @param dateUpdate Date update
     * @param status     0: End session | 1: In session
     */
    public Session(String sessionID, Date dateCreate, Date dateUpdate, int status) {
        this.sessionID = sessionID;
        this.dateCreate = dateCreate;
        this.dateUpdate = dateUpdate;
        this.status = status;
    }

    /**
     * Get account ID One
     *
     * @return account ID One
     */
    public String getAccountIDOne() {
        return accountIDOne;
    }

    /**
     * Set account ID One
     *
     * @param accountIDOne account ID One
     */
    public void setAccountIDOne(String accountIDOne) {
        this.accountIDOne = accountIDOne;
    }

    /**
     * Get account ID Two
     *
     * @return account ID Two
     */
    public String getAccountIDTwo() {
        return accountIDTwo;
    }

    /**
     * Set account ID Two
     *
     * @param accountIDTwo account ID Two
     */
    public void setAccountIDTwo(String accountIDTwo) {
        this.accountIDTwo = accountIDTwo;
    }

    /**
     * Compare and set the smaller accID to accIDOne
     *
     * @param accountIDOne accountIDOne
     * @param accountIDTwo accountIDTwo
     */
    public void setAccOneAndAccTwo(String accountIDOne, String accountIDTwo) {
        if (accountIDOne.compareTo(accountIDTwo) < 0) {
            this.accountIDOne = accountIDOne;
            this.accountIDTwo = accountIDTwo;
        } else {
            this.accountIDOne = accountIDTwo;
            this.accountIDTwo = accountIDOne;
        }
    }

    /**
     * Get Date create
     *
     * @return date create
     */
    public Date getDateCreate() {
        return dateCreate;
    }

    /**
     * Set Date create
     *
     * @param dateCreate date create
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
     * Get Session ID
     *
     * @return Session ID
     */
    public String getSessionID() {
        return sessionID;
    }

    /**
     * Set Session ID
     *
     * @param sessionID Session ID
     */
    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    /**
     * Get Status
     *
     * @return 0: End session | 1: In session
     */
    public int getStatus() {
        return status;
    }

    /**
     * Set Status. 0: End session | 1: In session
     *
     * @param status 0: End session | 1: In session
     */
    public void setStatus(int status) {
        this.status = status;
    }
}
