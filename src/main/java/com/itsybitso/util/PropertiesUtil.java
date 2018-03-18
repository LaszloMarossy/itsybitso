package com.itsybitso.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * populates properties into map.
 */
public class PropertiesUtil {

    static Properties prop = new Properties();
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtil.class);

    static {
        // Read properties file.
        try {
            prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "itsybitso." + System.getProperty("env") + ".properties"));
            //extra props not in file
            prop.setProperty("extra_property", "extra prop value");
        } catch (IOException e) {
            LOGGER.error("could not read properties from file itsybitso." + System.getProperty("env") + ".properties");
        }
    }

    public static String getProperty(String name) {
        return (String) prop.get(name);
    }

}
