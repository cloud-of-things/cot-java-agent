package com.telekom.cot.device.agent.operation.handler;

import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.service.configuration.Configuration;
import com.telekom.m2m.cot.restsdk.devicecontrol.Operation;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus;

public class OperationExecuteBuilderTest {

    private OperationExecuteBuilder builder;
    private Operation operation;
    private OperationExecuteCallback<String> callback;
    private Configuration configuration;

    @Before
    public void setUp() {
        operation = new Operation();
        configuration = new Configuration() {
        };
        callback = new OperationExecuteCallback<String>() {
            @Override
            public void finished(String content) throws AbstractAgentException {
            }
        };
        builder = OperationExecuteBuilder.create(operation);
    }

    @Test
    public void testPositivBuild() throws AbstractAgentException {
        // given
        builder.setExecutorClass(TestOperationWithExecute.class);
        builder.addParameter("test", "value");
        builder.setCallback(callback);
        builder.setConfiguration(configuration);
        TestOperationWithExecute execute = (TestOperationWithExecute) builder.build();
        // then
        Assert.assertThat(execute.getOperation(), Matchers.equalTo(operation));
        Assert.assertThat(execute.getCallback(), Matchers.equalTo(callback));
        Assert.assertThat(execute.getConfiguration(), Matchers.equalTo(configuration));
        Assert.assertThat(execute.getParams(), Matchers.notNullValue());
        Assert.assertThat(execute.getParams().get("test"), Matchers.equalTo("value"));
    }

    @Test
    public void testExecutorOperationExist() throws AbstractAgentException {
        // given executor class is null
        builder = OperationExecuteBuilder.create(null);
        try {
            // when build
            builder.build();
            Assert.fail();
        } catch (AbstractAgentException exception) {
            // then exception
        }
    }

    @Test
    public void testExecutorClassNotExist() throws AbstractAgentException {
        // given executor class is null
        builder.setExecutorClass(null);
        try {
            // when build
            builder.build();
            Assert.fail();
        } catch (AbstractAgentException exception) {
            // then exception
        }
    }

    @Test
    public void testDefaultConstructorOfExecutorClass() throws AbstractAgentException {
        // given executor class without default constructor
        builder.setExecutorClass(TestOperationWithoutExecute.class);
        // negative test
        try {
            // when build
            builder.build();
            Assert.fail();
        } catch (AbstractAgentException exception) {
            // then exception
        }
        // given executor class without default constructor
        builder.setExecutorClass(TestOperationWithExecute.class);
        // when build
        TestOperationWithExecute execute = (TestOperationWithExecute) builder.build();
        Assert.assertThat(execute.getOperation(), Matchers.equalTo(operation));
    }

    @SuppressWarnings("rawtypes")
    static class TestOperationWithoutExecute implements OperationExecute {

        private Operation operation;
        private Map params;
        private Configuration config;
        private OperationExecuteCallback<?> callback;

        public TestOperationWithoutExecute(String args) {

        }

        @Override
        public OperationStatus perform() throws AbstractAgentException {
            return OperationStatus.SUCCESSFUL;
        }

        @Override
        public void setOperation(Operation operation) {
            this.operation = operation;
        }

        @Override
        public void setParameter(Map params) {
            this.params = params;
        }

        @Override
        public void setConfiguration(Configuration config) {
            this.config = config;
        }

        @Override
        public void setCallback(OperationExecuteCallback callback) {
            this.callback = callback;
        }

        @Override
        public Configuration getConfiguration() {
            return config;
        }

        public Operation getOperation() {
            return operation;
        }

        public OperationExecuteCallback<?> getCallback() {
            return callback;
        }

        public Map getParams() {
            return params;
        }
    }

    static class TestOperationWithExecute extends TestOperationWithoutExecute {

        public TestOperationWithExecute() {
            super("");
        }

    }

}
