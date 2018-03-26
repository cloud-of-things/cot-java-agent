package com.telekom.cot.device.agent.common.util;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationUtil.class);

    private static ValidatorFactory factory;
    private static Validator validator;

    static {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private ValidationUtil() {
    }

    /**
     * checks if the given list and all objects in it are valid
     */
    public static boolean isValid(List<? extends Object> list) {
        for (Object object : list) {
            if (!isValid(object)) {
            	LOGGER.error("list is not valid");
                return false;
            }
        }

    	LOGGER.debug("list and its content is valid");
        return true;
    }

    /**
     * checks if the given object is valid
     */
    public static boolean isValid(Object object) {
    	// check object
    	if (Objects.isNull(object)) {
    		return false;
    	}
    	
    	// validate object
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object);
        if (constraintViolations.isEmpty()) {
        	LOGGER.debug("object '{}' is valid", object.getClass().getName());
        	return true;
        }
        
        // log all constraint violations
        for (ConstraintViolation<Object> v : constraintViolations) {
        	LOGGER.error("object is not valid: {} {}", v.getPropertyPath(), v.getMessage());
        }
        
        return false;
    }
}
