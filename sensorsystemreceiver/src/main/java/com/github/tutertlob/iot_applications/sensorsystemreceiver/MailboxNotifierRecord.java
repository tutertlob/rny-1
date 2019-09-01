package com.github.tutertlob.iot_applications.sensorsystemreceiver;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MailboxNotifierRecord {
    @SerializedName("event")
    @Expose
    private String event;

    @SerializedName("message")
    @Expose
    private String message;

    public MailboxNotifierRecord() {

    }

    public MailboxNotifierRecord(String event, String message) {
        this.event = event;
        this.message = message;
    }

    public String getEvent() {
        return event;
    }

    public String getMessage() {
        return message;
    }
}
