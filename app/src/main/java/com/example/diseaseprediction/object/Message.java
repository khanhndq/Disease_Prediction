package com.example.diseaseprediction.object;

import java.util.Date;

/**
 * Message
 */
public class Message {

    private String messageID;
    private String senderID;
    private String message;
    private Date dateSend;
    private String sessionID;
    private int status;

    /**
     * Init new Message
     */
    public Message() {
    }

    /**
     * Init new Message
     *
     * @param messageID Message ID
     * @param senderID  Sender ID
     * @param message   Message content
     * @param dateSend  Send date
     * @param sessionID Session ID
     * @param status    Message status
     */
    public Message(String messageID, String senderID, String message, Date dateSend,
                   String sessionID, int status) {
        this.messageID = messageID;
        this.senderID = senderID;
        this.message = message;
        this.dateSend = dateSend;
        this.sessionID = sessionID;
        this.status = status;
    }

    /**
     * Get Message ID
     *
     * @return message ID
     */
    public String getMessageID() {
        return messageID;
    }

    /**
     * set Message ID
     *
     * @param messageID message ID
     */
    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    /**
     * Get sender ID
     *
     * @return sender ID
     */
    public String getSenderID() {
        return senderID;
    }

    /**
     * Set sender ID
     *
     * @param senderID sender ID
     */
    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    /**
     * Get message content
     *
     * @return message content
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set message content
     *
     * @param message message content
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get send date
     *
     * @return send date
     */
    public Date getDateSend() {
        return dateSend;
    }

    /**
     * Set send date
     *
     * @param dateSend send date
     */
    public void setDateSend(Date dateSend) {
        this.dateSend = dateSend;
    }

    /**
     * Get session ID
     *
     * @return session ID
     */
    public String getSessionID() {
        return sessionID;
    }

    /**
     * Set session ID
     *
     * @param sessionID session ID
     */
    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    /**
     * Get Status
     *
     * @return 0: Deleted | 1: Normal | 2: Recommend symptom disable
     */
    public int getStatus() {
        return status;
    }

    /**
     * Set Status. 0: Deleted | 1: Normal | 2:??? | 3:???
     *
     * @param status 0: Deleted | 1: Normal | 2:??? | 3:???
     */
    public void setStatus(int status) {
        this.status = status;
    }
}
