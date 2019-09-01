package com.github.tutertlob.iot_applications.sensorsystemreceiver;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class JpegImageRecord {
    @SerializedName("event")
    @Expose
    private String event;

    @SerializedName("file")
    @Expose
    private String file;

    public JpegImageRecord() {

    }

    public JpegImageRecord(String event, String file) {
        this.event = event;
        this.file = file;
    }

    public String getEvent() {
        return event;
    }

    public String getMessage() {
        return file;
    }
}
