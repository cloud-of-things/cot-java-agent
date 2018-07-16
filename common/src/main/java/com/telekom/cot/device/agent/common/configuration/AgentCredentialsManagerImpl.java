package com.telekom.cot.device.agent.common.configuration;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.assertNotNull;
import static com.telekom.cot.device.agent.common.util.AssertionUtil.createExceptionAndLog;

import java.nio.file.Path;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentCredentialsNotFoundException;
import com.telekom.cot.device.agent.common.exc.AgentCredentialsWriteException;
import com.telekom.cot.device.agent.common.util.ValidationUtil;

public class AgentCredentialsManagerImpl implements AgentCredentialsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentCredentialsManagerImpl.class);
    private ConfigurationFile credentialsFile;
    private AgentCredentials credentials;
    private boolean existDeviceCredentialsFile;

    /**
     * hidden constructor, use "getInstance"
     * 
     * @param deviceCredentialsFile
     * @throws AbstractAgentException 
     */
    private AgentCredentialsManagerImpl(Path deviceCredentialsFile) throws AbstractAgentException {
        try {
            credentialsFile = ConfigurationFile.open(deviceCredentialsFile);
            existDeviceCredentialsFile = true;
        } catch (AbstractAgentException openExc) {
            LOGGER.info("yaml can't be oppened");
        }
        if (Objects.isNull(credentialsFile)) {
            try {
                credentialsFile = ConfigurationFile.create(deviceCredentialsFile);
            } catch (AbstractAgentException createExc) {
                LOGGER.error("yaml file can't be created");
                throw createExc;
            }
        }
        credentials = null;
    }

    /**
     * gets a new instance of
     * 
     * @param deviceCredentialsFile
     * @return the credentials manager
     * @throws AbstractAgentException 
     */
    public static AgentCredentialsManager getInstance(Path deviceCredentialsFile) throws AbstractAgentException {
        return new AgentCredentialsManagerImpl(deviceCredentialsFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AgentCredentials getCredentials() throws AbstractAgentException {
        LOGGER.debug("get agent credentials");
        if (Objects.nonNull(credentials)) {
            LOGGER.info("got existing agent credentials");
            return credentials;
        }
        return readCredentials();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCredentials(AgentCredentials credentials) {
        LOGGER.debug("set agent credentials");
        this.credentials = credentials;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AgentCredentials readCredentials() throws AbstractAgentException {
        // read agent credentials from yaml file and check
        AgentCredentials readCredentials = credentialsFile.getConfiguration(AgentCredentials.class);
        // is a new file created, then the credentials are in bootstrapping mode
        readCredentials.setBootstrappingMode(!existDeviceCredentialsFile);
        assertNotNull(readCredentials, AgentCredentialsNotFoundException.class, LOGGER, "can't read agent credentials from credentials file");
        // validate read credentials
        validateCredentials(readCredentials, AgentCredentialsNotFoundException.class);
        this.credentials = readCredentials;
        return credentials;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeCredentials(AgentCredentials credentials) throws AbstractAgentException {
        // validate credentials
        validateCredentials(credentials, AgentCredentialsWriteException.class);
        // put credentials to credentials file
        if (!credentialsFile.putConfiguration(credentials, true)) {
            createExceptionAndLog(AgentCredentialsWriteException.class, LOGGER, "can't write agent credentials to credentials file");
        }
        this.credentials = credentials;
        LOGGER.info("write credentials to credentials file successfully");
    }

    private void validateCredentials(AgentCredentials credentials,
                    Class<? extends AbstractAgentException> excepetionType) throws AbstractAgentException {
        // validate credentials
        if (!ValidationUtil.isValid(credentials)) {
            throw createExceptionAndLog(excepetionType, LOGGER, "agent credentials are not valid");
        }
    }
}
