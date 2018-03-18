package com.itsybitso.util;


public interface HealthCheck {

    HealthStatus check();
    String getName();
}
