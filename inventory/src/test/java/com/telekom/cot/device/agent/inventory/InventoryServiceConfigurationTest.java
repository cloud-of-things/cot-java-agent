package com.telekom.cot.device.agent.inventory;

import com.telekom.cot.device.agent.common.util.ValidationUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class InventoryServiceConfigurationTest {

    private static final String deviceName = "testDeviceName";
    private static final String deviceType = "testDeviceType";
    private static final boolean device    = true;
    private static final boolean agent     = true;

    private static final String expectedToString = "InventoryServiceConfiguration [deviceName="+deviceName+", deviceType="+deviceType+", device="+device+", agent="+agent+"]";

    InventoryServiceConfiguration inventoryServiceConfiguration;

    @Before
    public void setup() {
        inventoryServiceConfiguration = new InventoryServiceConfiguration();

        inventoryServiceConfiguration.setDeviceName(deviceName);
        inventoryServiceConfiguration.setDeviceType(deviceType);
        inventoryServiceConfiguration.setDevice(device);
        inventoryServiceConfiguration.setAgent(agent);
    }

    @Test
    public void testGetters() {
        Assert.assertEquals(deviceName, inventoryServiceConfiguration.getDeviceName());
        Assert.assertEquals(deviceType, inventoryServiceConfiguration.getDeviceType());
        Assert.assertEquals(device, inventoryServiceConfiguration.isDevice());
        Assert.assertEquals(agent, inventoryServiceConfiguration.isAgent());

        Assert.assertEquals(expectedToString, inventoryServiceConfiguration.toString());
    }

    @Test
    public void validate() {
        Assert.assertTrue(ValidationUtil.isValid(inventoryServiceConfiguration));
    }

    @Test
    public void notNullIsNullTest() {
        inventoryServiceConfiguration.setDeviceName(null);
        Assert.assertFalse(ValidationUtil.isValid(inventoryServiceConfiguration));
        inventoryServiceConfiguration.setDeviceName(deviceName);

        inventoryServiceConfiguration.setDeviceType(null);
        Assert.assertFalse(ValidationUtil.isValid(inventoryServiceConfiguration));
    }

    @Test
    public void emptyStringsTest() {
        inventoryServiceConfiguration.setDeviceName("");
        Assert.assertFalse(ValidationUtil.isValid(inventoryServiceConfiguration));

        inventoryServiceConfiguration.setDeviceName(deviceName);
        inventoryServiceConfiguration.setDeviceType("");
        Assert.assertFalse(ValidationUtil.isValid(inventoryServiceConfiguration));
    }


}
