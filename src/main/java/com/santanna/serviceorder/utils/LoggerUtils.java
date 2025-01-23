package com.santanna.serviceorder.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggerUtils {
    public Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    public void logInfo(Class<?> clazz, String message, Object... args) {
        Logger logger = getLogger(clazz);
        logger.info(message, args);
    }

    public void logError(Class<?> clazz, String message, Throwable ex, Object... args) {
        Logger logger = getLogger(clazz);
        logger.error(message, args, ex);
    }

    public void logWarn(Class<?> clazz, String message, Object... args) {
        Logger logger = getLogger(clazz);
        logger.warn(message, args);
    }

    public void logDebug(Class<?> clazz, String message, Object... args) {
        Logger logger = getLogger(clazz);
        logger.debug(message, args);
    }
}
