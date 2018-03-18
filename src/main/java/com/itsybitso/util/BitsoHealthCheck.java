package com.itsybitso.util;


/**
 * checks if the connection to Bitso
 */
public class BitsoHealthCheck implements HealthCheck {

    @Override
    public HealthStatus check() {

        try {
            // try connecting to Bitso
        } catch (Exception e) {
            return HealthStatus.unhealthy(e.getMessage());
        }
        return HealthStatus.healthy();
    }

    @Override
    public String getName() {
        return "BitsoHealth";
    }
}
