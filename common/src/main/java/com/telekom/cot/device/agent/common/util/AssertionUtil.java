package com.telekom.cot.device.agent.common.util;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;

public class AssertionUtil {

    /**
     * private default constructor to hide implicit constructor
     */
    private AssertionUtil() {
    }
    
    /**
     * checks the given object and logs an error and throws an exception of given type when object is {@code null}  
     * @param object object to check
     * @param exceptionType type of exception to throw if object is {@code null}
     * @param logger logger instance to log the error message
     * @param errorMessage message to log and to use in the exception
     * @throws AbstractAgentException if object is {@code null}
     */
    public static void assertIsTrue(Boolean trueValue, Class<? extends AbstractAgentException> exceptionType, Logger logger, String errorMessage) throws AbstractAgentException {
        if(!trueValue) {
            throw createExceptionAndLog(exceptionType, logger, errorMessage);
        }
    }
    
    /**
     * checks the given object and logs an error and throws an exception of given type when object is {@code null}  
     * @param object object to check
     * @param exceptionType type of exception to throw if object is {@code null}
     * @param logger logger instance to log the error message
     * @param errorMessage message to log and to use in the exception
     * @throws AbstractAgentException if object is {@code null}
     */
    public static void assertNotNull(Object object, Class<? extends AbstractAgentException> exceptionType, Logger logger, String errorMessage) throws AbstractAgentException {
    	if(Objects.isNull(object)) {
    		throw createExceptionAndLog(exceptionType, logger, errorMessage);
    	}
    }

    /**
     * checks the given string and logs an error and throws an exception of given type when string is {@code null} or empty  
     * @param string string to check
     * @param exceptionType type of exception to throw if string is {@code null} or empty
     * @param logger logger instance to log the error message
     * @param errorMessage message to log and to use in the exception
     * @throws AbstractAgentException if string is {@code null} or empty
     */
    public static void assertNotEmpty(String string, Class<? extends AbstractAgentException> exceptionType, Logger logger, String errorMessage) throws AbstractAgentException {
    	if(StringUtils.isEmpty(string)) {
    		throw createExceptionAndLog(exceptionType, logger, errorMessage);
    	}
    }

    /**
     * checks the given map and logs an error and throws an exception of given type when string is {@code null} or empty  
     * @param map string to check
     * @param exceptionType type of exception to throw if string is {@code null} or empty
     * @param logger logger instance to log the error message
     * @param errorMessage message to log and to use in the exception
     * @throws AbstractAgentException if map is {@code null} or empty
     */
    public static void assertNotEmpty(Map<?, ?> map, Class<? extends AbstractAgentException> exceptionType, Logger logger, String errorMessage) throws AbstractAgentException {
        if(Objects.isNull(map) || map.isEmpty()) {
            throw createExceptionAndLog(exceptionType, logger, errorMessage);
        }
    }

    /**
     * creates an exception of given type, logs an error @ logger
     * @param exceptionType type of exception to throw
     * @param logger logger instance to log the error message
     * @param errorMessage message to log and to use in the exception
     * @param throwable instance of {@link Throwable} to pass to exception 
     * @return the created exception
     */
	public static AbstractAgentException createExceptionAndLog(Class<? extends AbstractAgentException> exceptionType, Logger logger, String errorMessage, Throwable throwable) {
    	AbstractAgentException exception = createException(exceptionType, errorMessage, throwable);

       	if(Objects.nonNull(logger)) {
       		if(Objects.nonNull(throwable)) {
       			logger.error(exception.getMessage(), throwable);
       		} else {
       			logger.error(exception.getMessage());
       		}
       	}
        	
        return exception;
    }
    
    /**
     * creates an exception of given type, logs an error @ logger
     * @param exceptionType type of exception to throw
     * @param logger logger instance to log the error message
     * @param errorMessage message to log and to use in the exception
     * @return the created exception
     */
	public static AbstractAgentException createExceptionAndLog(Class<? extends AbstractAgentException> exceptionType, Logger logger, String errorMessage) {
		return createExceptionAndLog(exceptionType, logger, errorMessage, null);
	}
	
    /**
     * try to create an AbstractAgentException by given exception type, error message and optional throwable
     */
    @SuppressWarnings("serial")
	private static AbstractAgentException createException(Class<? extends AbstractAgentException> exceptionType, String errorMessage, Throwable throwable) {
    	try {
        	Constructor<? extends AbstractAgentException> constructor;

        	if (Objects.nonNull(throwable)) {
        		constructor = exceptionType.getConstructor(String.class, Throwable.class);
        		return constructor.newInstance(errorMessage, throwable); 
    		} else {
        		constructor = exceptionType.getConstructor(String.class);
        		return constructor.newInstance(errorMessage); 
    		}
    	} catch(Exception e) {
    		return new AbstractAgentException("can't create exception of type " + exceptionType.getName(), e) {};
    	}
    }
}
