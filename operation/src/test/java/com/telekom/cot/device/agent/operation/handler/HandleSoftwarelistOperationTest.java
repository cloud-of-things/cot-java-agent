package com.telekom.cot.device.agent.operation.handler;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.telekom.cot.device.agent.common.ChecksumAlgorithm;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.util.InjectionUtil;
import com.telekom.cot.device.agent.operation.softwareupdate.SoftwareUpdateConfig;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.m2m.cot.restsdk.devicecontrol.Operation;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus;

public class HandleSoftwarelistOperationTest {

    private PlatformService mockPlatformService;
    private AgentOperationsHandlerService service;
    private Runtime mockRuntime;
    private Path currentRelativePath;

    @Before
    public void setUp() throws IOException {
        // mock
        mockPlatformService = Mockito.mock(PlatformService.class);
        mockRuntime = Mockito.mock(Runtime.class);
        // conf
        AgentOperationsHandlerConfiguration config = new AgentOperationsHandlerConfiguration();
        SoftwareUpdateConfig softwareUpdateConfig = new SoftwareUpdateConfig();
        softwareUpdateConfig.setChecksumAlgorithm(ChecksumAlgorithm.MD5);
        currentRelativePath = Files.createTempDirectory("zip_test");
        config.setSoftwareUpdate(softwareUpdateConfig);
        // service
        service = new TestAgentOperationsHandlerService(mockPlatformService);
        InjectionUtil.inject((AgentOperationsHandlerService) service, config);
    }

    @After
    public void tearDown() {
        if (currentRelativePath != null) {
            deleteDirectory(currentRelativePath.toFile());
        }
    }

    @Ignore
    @Test
    public void testHandleSoftwarelistOperation() throws AbstractAgentException, IOException, URISyntaxException {
        // given operation
        JsonObject software = new JsonObject();
        software.addProperty("name", "softwareName");
        software.addProperty("version", "1.2.3");
        software.addProperty("url", "http://www.execute.com");
        
        JsonArray softwareList = new JsonArray();
        softwareList.add(software);

        Operation operation = new Operation();
        operation.set("c8y_SoftwareList", softwareList);
        
        // when execute handleSoftwarelistOperation
        String agentYamlDir = Paths.get(HandleSoftwarelistOperationTest.class.getResource("/agent.yaml").toURI())
                        .toString();
        agentYamlDir = agentYamlDir.substring(0, agentYamlDir.lastIndexOf("agent.yaml") - 1);
        OperationStatus status = service.handleSoftwarelistOperation(operation, agentYamlDir, mockRuntime);
        // then the status is EXECUTING
        Assert.assertThat(status, Matchers.equalTo(OperationStatus.EXECUTING));
        // check copy of agent.yaml file
        String agentYamlFile = Paths.get(currentRelativePath.toString(), "agent.yaml").toString();
        Assert.assertThat(agentYamlFile.endsWith("agent.yaml"), Matchers.equalTo(true));
        // verify platform call
        Mockito.verify(mockPlatformService).downloadBinary(Mockito.any());
    }

    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allFiles = directoryToBeDeleted.listFiles();
        if (allFiles != null) {
            for (File file : allFiles) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}
