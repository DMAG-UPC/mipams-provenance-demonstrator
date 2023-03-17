package org.mipams.provenance.demo.entities.responses;

import java.util.Date;

public class ApiError {
    private Date timestamp;
    private String errorMessage;
    private String httpCodeMessage;

    public ApiError(Date timestamp, String errorMessage, String httpCodeMessage) {
        this.timestamp = timestamp;
        this.errorMessage = errorMessage;
        this.httpCodeMessage = httpCodeMessage;
    }

    public String getHttpCodeMessage() {
        return httpCodeMessage;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}