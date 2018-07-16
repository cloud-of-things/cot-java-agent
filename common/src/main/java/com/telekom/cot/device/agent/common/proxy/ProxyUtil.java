package com.telekom.cot.device.agent.common.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyUtil.class);

    /**
     * private default constructor to hide implicit constructor
     */
    private ProxyUtil() {
    }
    
    /**
     * create a proxy
     * @param classType the class from which a proxy is generated
     * @param invocationHandler the proxy invocation handler
     * @return the proxy if found interfaces or {@code null} if not found
     */
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(Class<T> classType, InvocationHandler invocationHandler) {
        if(Objects.isNull(classType)) {
            LOGGER.error("no class type given");
            return null;
        }
        
        List<Class<?>> interfaces = getInterfaces(classType);
        if (interfaces.isEmpty()) {
            LOGGER.error("no interfaces given");
            return null;
        }
        
        return (T) Proxy.newProxyInstance(classType.getClassLoader(), interfaces.toArray(new Class<?>[0]), invocationHandler);
    }

    private static List<Class<?>> getInterfaces(Class<?> classType) {
        // create new list, add this if it's an interface
        List<Class<?>> interfaces = new ArrayList<>();
        if(classType.isInterface()) {
            interfaces.add(classType);
        }
        
        // add interfaces implemented by this class
        for (Class<?> directInterface : classType.getInterfaces()) {
            interfaces.add(directInterface);
        }
        
        // add interfaces of superclass
        Class<?> superClass = classType.getSuperclass();
        if (Objects.nonNull(superClass)) {
            interfaces.addAll(getInterfaces(superClass));
        }
        
        return interfaces;
    }
}
