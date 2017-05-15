package org.apache.jmeter.visualizers.backend;

import org.apache.jmeter.samplers.SampleResult;

public class ErrorMetric {

    private String responseCode = "";
    private String responseMessage = "";

    public ErrorMetric() {
    }

    public ErrorMetric(SampleResult result) {
        responseCode = result.getResponseCode();
        responseMessage = result.getResponseMessage();
    }

    public String getResponseCode() {
        if (responseCode.isEmpty()) {
            return "0";
        } else {
            return responseCode;
        }
    }

    public String getResponseMessage() {
        if (responseMessage.isEmpty()) {
            return "None";
        } else {
            return responseMessage;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ErrorMetric)) {
            return false;
        }

        ErrorMetric otherError = (ErrorMetric) other;
        if (getResponseCode().equalsIgnoreCase(otherError.getResponseCode())
                && getResponseMessage().equalsIgnoreCase(otherError.getResponseMessage())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getResponseCode().hashCode() + getResponseMessage().hashCode();
    }

}
