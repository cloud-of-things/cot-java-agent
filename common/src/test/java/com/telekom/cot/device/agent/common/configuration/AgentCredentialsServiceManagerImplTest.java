package com.telekom.cot.device.agent.common.configuration;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;

public class AgentCredentialsServiceManagerImplTest {

    private static final String READ_TEST_FILE = "./src/test/resources/readCredentialsTest.yaml";
    private static final String WRITE_TEST_FILE = "./src/test/resources/writeCredentialsTest.yaml";

    private static final String USERNAME = "testUsername";
    private static final String PASSWORD = "testPassword";
    private static final String TENANT = "testTenant";
    private static final String WRITE_TEST_USERNAME = "writeTestUsername";
    private static final String WRITE_TEST_PASSWORD = "writeTestPassword";
    private static final String WRITE_TEST_TENANT = "writeTestTenant";

    private AgentCredentialsManager agentCredentialsManager;

    @Before
    public void setUp() throws AbstractAgentException {
        agentCredentialsManager = AgentCredentialsManagerImpl.getInstance(Paths.get(READ_TEST_FILE));
    }

    @After
    public void tearDown() {
        try {
            Files.deleteIfExists(Paths.get(WRITE_TEST_FILE));
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testGetter() throws AbstractAgentException {
        assertEquals(USERNAME, agentCredentialsManager.getCredentials().getUsername());
        assertEquals(PASSWORD, agentCredentialsManager.getCredentials().getPassword());
        assertEquals(TENANT, agentCredentialsManager.getCredentials().getTenant());
    }

    @Test
    public void testSetter() throws AbstractAgentException {
        AgentCredentials agentCredentials = new AgentCredentials(true,WRITE_TEST_TENANT, WRITE_TEST_USERNAME, WRITE_TEST_PASSWORD);
        agentCredentialsManager.setCredentials(agentCredentials);
        assertEquals(WRITE_TEST_TENANT, agentCredentialsManager.getCredentials().getTenant());
        assertEquals(WRITE_TEST_USERNAME, agentCredentialsManager.getCredentials().getUsername());
        assertEquals(WRITE_TEST_PASSWORD, agentCredentialsManager.getCredentials().getPassword());
    }

    @Test
    public void testWriteCredentials() throws AbstractAgentException {
        //write new credentials
        AgentCredentials agentCredentials = new AgentCredentials(true, WRITE_TEST_TENANT, WRITE_TEST_USERNAME, WRITE_TEST_PASSWORD);
        agentCredentialsManager = AgentCredentialsManagerImpl.getInstance(Paths.get(WRITE_TEST_FILE));
        agentCredentialsManager.writeCredentials(agentCredentials);

        //read new credentials and validate them
        AgentCredentials readAgentCredentials = agentCredentialsManager.readCredentials();
        assertEquals(WRITE_TEST_TENANT, readAgentCredentials.getTenant());
        assertEquals(WRITE_TEST_USERNAME, readAgentCredentials.getUsername());
        assertEquals(WRITE_TEST_PASSWORD, readAgentCredentials.getPassword());
    }
}
