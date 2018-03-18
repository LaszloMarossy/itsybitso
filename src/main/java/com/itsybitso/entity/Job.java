package com.itsybitso.entity;

import java.io.Serializable;

public class Job implements Serializable {

    private static final long serialVersionUID = 8287801093151786196L;

    private long jobId;
    private int jobStatus;
    private int runStatus;
    private String jobParams;


    public Job(long jobId, int jobStatus, int runStatus, String jobParams) {
        this.jobId = jobId;
        this.jobStatus = jobStatus;
        this.runStatus = runStatus;
        this.jobParams = jobParams;
    }

    public Job() {    }

    public int getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(int jobStatus) {
        this.jobStatus = jobStatus;
    }

    public int getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(int runStatus) {
        this.runStatus = runStatus;
    }

    public String getJobParams() {
        return jobParams;
    }

    public long getJobId() {
        return jobId;
    }
}
