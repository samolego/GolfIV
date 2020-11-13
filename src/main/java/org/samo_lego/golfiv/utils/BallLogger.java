package org.samo_lego.golfiv.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BallLogger {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void logInfo(String msg) {
        LOGGER.info(msg);
    }

    public static void logError(String msg) {
        LOGGER.error(msg);
    }
}
