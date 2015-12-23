package com.amigos.model;

/**
 * Created by Nirav on 12/12/2015.
 */
public class JobInfo {

    String jobId;
    String jobStatus;
    String jobDetails;

    public JobInfo(String jobId, String jStatus, String details) {
        this.jobId = jobId;
        this.jobStatus = jStatus;
        this.jobDetails = details;
    }


    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getJobDetails() {
        return jobDetails;
    }
}
