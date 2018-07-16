package com.telekom.cot.device.agent.common.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InjectionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(InjectionUtil.class);

    /**
     * private default constructor to hide implicit constructor
     */
    private InjectionUtil() {
    }
    
    /**
     * searches all fields annotated with given annotation type and of given field type at given class 
     * 
     * @param classType
     *            type of class to search the field in
     * @param annotationType
     *            annotation of the field to search
     * @param fieldType
     *            type of the field to search
     * @return the fields if found or empty list if not found
     */
    public static List<Field> getFields(Class<?> classType, Class<? extends Annotation> annotationType, Class<?> fieldType) {
        List<Field> fields = new ArrayList<>();

        // check class type
        if (Objects.isNull(classType)) {
            LOGGER.error("no class type given");
            return fields;
        }
        
        // check annotation type
        if (Objects.isNull(annotationType)) {
            LOGGER.error("no annotation type given");
            return fields;
        }

        // get and check super class, get fields from super class
        Class<?> superClassType = classType.getSuperclass();
        if (Objects.nonNull(superClassType)) {
            fields.addAll(getFields(superClassType, annotationType, fieldType));
        }
        
        for (Field field : classType.getDeclaredFields()) {
            // check whether field is annotated
            if (!field.isAnnotationPresent(annotationType)) {
                continue;
            }
            
            // when specific field type is requested, check type
            if (Objects.nonNull(fieldType) && fieldType.isAssignableFrom(field.getType())
                            || Objects.isNull(fieldType)) {
                fields.add(field);
            }
        }
        
        return fields;
    }

    /**
     * searches all fields annotated with given annotation type at given class 
     * 
     * @param classType
     *            type of class to search the field in
     * @param annotationType
     *            annotation of the field to search
     * @return a list of annotated fields, empty if no annotated field found
     */
    public static List<Field> getFields(Class<?> classType, Class<? extends Annotation> annotationType) {
        return getFields(classType, annotationType, null);
    }

    /**
     * searches a field with given name in the class of given type (including super classes)
     * 
     * @param classType
     *            type of class to search the field in
     * @param fieldName
     *            name of the field to search
     * @return the field if found or {@code null} if not found
     */
    public static Field getField(Class<?> classType, String fieldName) {
        // check field name
        if (StringUtils.isEmpty(fieldName)) {
            LOGGER.error("no field name given");
            return null;
        }
        // search class and super classes
        Class<?> classtoSearchIn = classType;
        while (Objects.nonNull(classtoSearchIn)) {
            try {
                return classtoSearchIn.getDeclaredField(fieldName);
            } catch (Exception e) {
                // search in next super class
                classtoSearchIn = classtoSearchIn.getSuperclass();
                LOGGER.debug("declared field '{}' not found by {}, get next super class", fieldName, e.getClass());
            }
        }
        return null;
    }

    /**
     * searches the first field with given type in the class of given type (including super classes)
     * 
     * @param classType
     *            type of class to search the field in
     * @param fieldType
     *            type of the field to search
     * @return the field if found or {@code null} if not found
     */
    public static Field getField(Class<?> classType, Class<?> fieldType) {
        // check class type
        if (Objects.isNull(classType)) {
            LOGGER.error("no class type given");
            return null;
        }

        // check field type
        if (Objects.isNull(fieldType)) {
            LOGGER.error("no field type given");
            return null;
        }

        // search class and super classes
        return searchField(classType, fieldType);
    }

    /**
     * helper method to inject an object instance into a class field
     * 
     * @param classInstance
     *            instance of a class where to inject a field object
     * @param fieldName
     *            name of the field to inject the object into
     * @param objectToInject
     *            the object instance to inject
     * @return whether injecting has been successful
     */
    public static boolean inject(Object classInstance, String fieldName, Object objectToInject) {
        // check parameters
        if (Objects.isNull(classInstance)) {
            LOGGER.error("no class instance given to inject");
            return false;
        }
        // get field by field name and inject object
        Field field = getField(classInstance.getClass(), fieldName);
        return injectObjectIntoField(field, classInstance, objectToInject);
    }

    /**
     * helper method to inject an object instance into a class field
     * 
     * @param classInstance
     *            instance of a class where to inject a field object
     * @param objectToInject
     *            the object instance to inject
     * @return whether injecting has been successful
     */
    public static boolean inject(Object classInstance, Object objectToInject) {
        // check class instance
        if (Objects.isNull(classInstance)) {
            LOGGER.error("no class instance given to inject");
            return false;
        }
        // check object to inject
        if (Objects.isNull(objectToInject)) {
            LOGGER.error("no object to inject given");
            return false;
        }
        // get field by object type and inject object
        Field field = getField(classInstance.getClass(), objectToInject.getClass());
        return injectObjectIntoField(field, classInstance, objectToInject);
    }

    /**
     * helper method to inject an object instance into a static class field
     * 
     * @param classType
     *            type of class where to inject a static field object
     * @param fieldName
     *            name of the static field to inject the object into
     * @param objectToInject
     *            the object instance to inject
     * @return whether injecting has been successful
     */
    public static boolean injectStatic(Class<?> classType, String fieldName, Object objectToInject) {
        // get field by field name and inject object
        Field field = getField(classType, fieldName);
        return injectObjectIntoField(field, null, objectToInject);
    }

    /**
     * helper method to inject an object instance into a static class field
     * 
     * @param classType
     *            type of class where to inject a static field object
     * @param objectToInject
     *            the object instance to inject
     * @return whether injecting has been successful
     */
    public static boolean injectStatic(Class<?> classType, Object objectToInject) {
        // check object to inject
        if (Objects.isNull(objectToInject)) {
            LOGGER.error("no object to inject given");
            return false;
        }
        // get field by object type and inject object
        Field field = getField(classType, objectToInject.getClass());
        return injectObjectIntoField(field, null, objectToInject);
    }

    /**
     * searches for a field of given type in the given class type
     */
    private static Field searchField(Class<?> classType, Class<?> fieldType) {
        // search in given class and its super classes
        Class<?> classToSearchIn = classType;
        while (Objects.nonNull(classToSearchIn)) {
            // search all fields in current class
            for (Field field : classToSearchIn.getDeclaredFields()) {
                if (field.getType().isAssignableFrom(fieldType)) {
                    return field;
                }
            }
            // search in next super class
            classToSearchIn = classToSearchIn.getSuperclass();
        }
        return null;
    }

    /**
     * injects the given object into the given field at given class instance
     */
    private static boolean injectObjectIntoField(Field field, Object classInstance, Object objectToInject) {
        // check field
        if (Objects.isNull(field)) {
            LOGGER.error("no field given");
            return false;
        }
        // remove final modifier
        FieldUtils.removeFinalModifier(field, true);
        // inject object
        try {
            field.setAccessible(true);
            field.set(classInstance, objectToInject);
            return true;
        } catch (Exception e) {
            LOGGER.error("can't inject object into field", e);
            return false;
        }
    }
}
