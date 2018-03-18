package com.itsybitso.bl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * base class to keep common code for business logic classes.
 */
public class BusinessLogic {

    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessLogic.class);

    public BusinessLogic() {

    }

    public void cleanup() throws Exception {

        try {
            // common cleanup task for all jobs
            LOGGER.info("common cleanup task for all jobs");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void setup() throws Exception {

        try {
            // common setup task for all jobs
            LOGGER.info("common setup task for all jobs");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


}
