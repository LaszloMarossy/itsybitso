package com.itsybitso.util;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.util.Objects;

/**
 * Health check impl
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class HealthStatus implements Serializable {

    protected Health health;
    protected String detail;

    // for JSON deserializing
    protected HealthStatus() { }

    protected HealthStatus(Health health, String detail) {
        this.health = health;
        this.detail = detail;
    }

    public Health getHealth() {
        return health;
    }

    public String getDetail() {
        return detail;
    }

    public static HealthStatus healthy() {
        return new HealthStatus(Health.HEALTHY, null);
    }

    public static HealthStatus unhealthy(String statusDetail) {
        return new HealthStatus(Health.UNHEALTHY, statusDetail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(health, detail);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final HealthStatus other = (HealthStatus) obj;
        return Objects.equals(this.health, other.health) && Objects.equals(this.detail, other.detail);
    }

    @Override
    public String toString() {
        return "HealthStatus{"
                + "health=" + health
                + ", detail='" + detail + "'"
                + "}";
    }
}
