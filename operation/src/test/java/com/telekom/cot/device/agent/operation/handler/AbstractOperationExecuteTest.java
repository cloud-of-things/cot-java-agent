package com.telekom.cot.device.agent.operation.handler;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.service.configuration.Configuration;
import com.telekom.m2m.cot.restsdk.devicecontrol.Operation;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus;

public class AbstractOperationExecuteTest {

    private TestConfiguration conf;
    private TestOperationExecute execute;
    private TestCallback callback;
    private Map<String, Object> params;

    @Before
    public void setUp() {
        conf = new TestConfiguration();
        execute = new TestOperationExecute();
        callback = new TestCallback();
        params = new HashMap<>();
        params.put("int", 1);
        params.put("string", "test");
    }

    @Test
    public void testSetterAndGetter() {

        execute.setCallback(callback);
        execute.setConfiguration(conf);
        execute.setOperation(new Operation());
        execute.setParameter(params);

        Assert.assertThat(execute.getParamValueAsInteger("int"), Matchers.equalTo(1));
        Assert.assertThat(execute.getParamValueAsString("string"), Matchers.equalTo("test"));
        Assert.assertThat(execute.getCallback(String.class), Matchers.notNullValue());
        Assert.assertThat(execute.getConfiguration(), Matchers.notNullValue());
        Assert.assertThat(execute.getOperation(), Matchers.notNullValue());
        Assert.assertThat(execute.getParams(), Matchers.notNullValue());

    }

    @Test
    public void testCallbackGenerics() throws AbstractAgentException {

        execute.setCallback(callback);
        execute.getCallback(String.class).finished("");
        try {
            execute.getCallback(Integer.class).finished(1);
            Assert.fail();
        } catch (AbstractAgentException exc) {

        }
    }

    class TestOperationExecute extends AbstractOperationExecute<TestConfiguration> {
        @Override
        public OperationStatus perform() throws AbstractAgentException {
            return OperationStatus.SUCCESSFUL;
        }

    }

    class TestConfiguration implements Configuration {

    }

    class TestCallback implements OperationExecuteCallback<String> {
        @Override
        public void finished(String content) throws AbstractAgentException {

        }
    }

}
