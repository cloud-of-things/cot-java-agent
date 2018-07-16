package com.telekom.cot.device.agent.common.proxy;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.hamcrest.Matchers;
import org.junit.Test;

public class ProxyUtilTest {

    @Test
    public void createProxyTestByInterface() {
        ProxyTestService proxyTestService = new ProxyTestService();
        InvocationHandler invocationHandler = new InvocationHandler() {
            
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object result = method.invoke(proxyTestService, args);
                return result;
            }
        }; 
        TestService proxy = ProxyUtil.createProxy(TestService.class, invocationHandler);
        assertThat(proxy.doSomeThing(),Matchers.equalTo("hello world"));
    }

    @Test
    public void createProxyTestByInstance() {
        ProxyTestService proxyTestService = new ProxyTestService();
        InvocationHandler invocationHandler = new InvocationHandler() {
            
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object result = method.invoke(proxyTestService, args);
                return result;
            }
        }; 
        TestService proxy = ProxyUtil.createProxy(proxyTestService.getClass(), invocationHandler);
        assertThat(proxy.doSomeThing(),Matchers.equalTo("hello world"));
    }
    
    public class ProxyTestService extends AbstractTestService implements Comparable<String> {
        @Override
        public String doSomeThing() {
            return "hello world";
        }
        @Override
        public int compareTo(String o) {
            return 0;
        }
    }

    public abstract class AbstractTestService implements TestService {
        @Override
        public String doSomeThing() {
            return "hello";
        }
    }

    public interface TestService {
        public String doSomeThing();
    }
}
