package com.itsybitso.util;

import java.io.Serializable;

/**
 * Result for health check impl
 */
public class HealthResult implements Serializable {

    public String healthCheckName;
    public HealthStatus healthStatus;

    protected HealthResult() { }

    public HealthResult(String healthCheckName, HealthStatus healthStatus) {
        this.healthCheckName = healthCheckName;
        this.healthStatus = healthStatus;
    }

    @Override
    public String toString() {
        return "HealthResult{"
                + "healthCheckName='" + healthCheckName + "'"
                + ", healthStatus=" + healthStatus
                + "}";
    }
}
