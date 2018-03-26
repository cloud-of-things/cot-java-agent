package com.telekom.cot.device.agent.inventory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.system.SystemService;
import com.telekom.cot.device.agent.system.properties.ConfigurationProperties;
import com.telekom.cot.device.agent.system.properties.FirmwareProperties;
import com.telekom.cot.device.agent.system.properties.HardwareProperties;
import com.telekom.cot.device.agent.system.properties.MobileProperties;
import com.telekom.cot.device.agent.system.properties.Properties;
import com.telekom.cot.device.agent.system.properties.SoftwareProperties;
import com.telekom.m2m.cot.restsdk.inventory.ManagedObject;
import com.telekom.m2m.cot.restsdk.library.Fragment;
import com.telekom.m2m.cot.restsdk.library.devicemanagement.Configuration;
import com.telekom.m2m.cot.restsdk.library.devicemanagement.Firmware;
import com.telekom.m2m.cot.restsdk.library.devicemanagement.Hardware;
import com.telekom.m2m.cot.restsdk.library.devicemanagement.IsDevice;
import com.telekom.m2m.cot.restsdk.library.devicemanagement.SoftwareList;
import com.telekom.m2m.cot.restsdk.library.sensor.Mobile;

public class ManagedObjectFactory {

    private static final String LOG_CREATE_FRAGMENT = "create fragment {}";
    private static final String NOT_AVAILABLE = "-";
    /** the logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private enum FragmentIdentifier {
        HARDWARE("c8y_Hardware"), CONFIGURATION("c8y_Configuration"), FIRMWARE("c8y_Firmware"), MOBILE(
                        "c8y_Mobile"), SOFTWARE_LIST("c8y_SoftwareList");

        private String id;

        FragmentIdentifier(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    private final SystemService systemService;
    private final InventoryServiceConfiguration config;

    private ManagedObjectFactory(InventoryServiceConfiguration config, SystemService systemService) {
        this.systemService = systemService;
        this.config = config;
    }

    public static ManagedObjectFactory getInstance(InventoryServiceConfiguration config, SystemService systemService) {
        return new ManagedObjectFactory(config, systemService);
    }

    public ManagedObject create(String name) {
        return initManagedObject(name, config, new ManagedObject());
    }

    public ManagedObject create() {
        return initManagedObject(null, config, new ManagedObject());
    }

    private ManagedObject initManagedObject(String name, InventoryServiceConfiguration config,
                    ManagedObject managedObject) {
        if (Objects.isNull(name)) {
            managedObject.setName(config.getDeviceName());
        } else {
            managedObject.setName(name);
        }
        managedObject.setType(config.getDeviceType());
        // set as device (is able to send measurements)
        if (config.isDevice()) {
            managedObject.addFragment(new IsDevice());
        }
        // set as agent (is able to execute operations)
        if (config.isAgent()) {
            managedObject.set("com_cumulocity_model_Agent", new JsonObject());
        }
        // hardware properties
        addFragment(managedObject, createHardware(getProperties(HardwareProperties.class)));
        // configuration properties
        addFragment(managedObject, createConfiguration(getProperties(ConfigurationProperties.class)));
        // firmware properties
        addFragment(managedObject, createFirmware(getProperties(FirmwareProperties.class)));
        // mobile properties
        addFragment(managedObject, createMobile(getProperties(MobileProperties.class)));
        // software list properties
        addFragment(managedObject, createSoftware(getProperties(SoftwareProperties.class)));
        return managedObject;
    }

    private <T extends Properties> T getProperties(Class<T> clazz) {
        LOGGER.info("get system properties {}", clazz.getName());
        try {
            return systemService.getProperties(clazz);
        } catch (AbstractAgentException e) {
            LOGGER.info("did not find system properties {}", clazz.getName(), e);
            return null;
        }
    }

    private void addFragment(ManagedObject managedObject, Fragment fragment) {
        LOGGER.info("add fragment {}", fragment.getId());
        managedObject.addFragment(fragment);
    }

    private Fragment createHardware(HardwareProperties hardwareProperties) {
        if (Objects.isNull(hardwareProperties)) {
            return new EmptyFragment(FragmentIdentifier.HARDWARE);
        }
        LOGGER.info(LOG_CREATE_FRAGMENT, hardwareProperties);
        return new Hardware(check(hardwareProperties.getModel(), NOT_AVAILABLE),
                        check(hardwareProperties.getRevision(), NOT_AVAILABLE),
                        check(hardwareProperties.getSerialNumber(), NOT_AVAILABLE));
    }

    private Fragment createConfiguration(ConfigurationProperties configurationProperties) {
        if (Objects.isNull(configurationProperties)) {
            return new EmptyFragment(FragmentIdentifier.CONFIGURATION);
        }
        LOGGER.info(LOG_CREATE_FRAGMENT, configurationProperties);
        return new Configuration(configurationProperties.getConfig());
    }

    private Fragment createFirmware(FirmwareProperties firmwareProperties) {
        if (Objects.isNull(firmwareProperties)) {
            return new EmptyFragment(FragmentIdentifier.FIRMWARE);
        }
        LOGGER.info(LOG_CREATE_FRAGMENT, firmwareProperties);
        return new Firmware(check(firmwareProperties.getName(), NOT_AVAILABLE),
                        check(firmwareProperties.getVersion(), NOT_AVAILABLE),
                        check(firmwareProperties.getUrl(), NOT_AVAILABLE));
    }

    private Fragment createMobile(MobileProperties mobileProperties) {
        if (Objects.isNull(mobileProperties)) {
            return new EmptyFragment(FragmentIdentifier.MOBILE);
        }
        LOGGER.info(LOG_CREATE_FRAGMENT, mobileProperties);
        return new Mobile(check(mobileProperties.getImei(), NOT_AVAILABLE),
                        check(mobileProperties.getCellId(), NOT_AVAILABLE),
                        check(mobileProperties.getIccid(), NOT_AVAILABLE));
    }

    private Fragment createSoftware(SoftwareProperties softwareProperties) {
        if (Objects.isNull(softwareProperties) || CollectionUtils.isEmpty(softwareProperties.getSoftwareList())) {
            return new EmptyFragment(FragmentIdentifier.SOFTWARE_LIST);
        }
        LOGGER.info(LOG_CREATE_FRAGMENT, softwareProperties);
        List<SoftwareList.Software> softwares = softwareProperties.getSoftwareList().stream()
                        .map(s -> new SoftwareList.Software(s.getName(), s.getVersion(), s.getUrl()))
                        .collect(Collectors.toList());
        return new SoftwareList(softwares.toArray(new SoftwareList.Software[] {}));
    }

    private String check(String value, String defaultValue) {
        return Objects.isNull(value) || value.isEmpty()  ? defaultValue : value;
    }

    /**
     * Create an empty fragment.
     *
     */
    static class EmptyFragment implements Fragment {

        private FragmentIdentifier identifier;

        public EmptyFragment(FragmentIdentifier identifier) {
            this.identifier = identifier;
        }

        @Override
        public String getId() {
            return identifier.getId();
        }

        @Override
        public JsonElement getJson() {
            return new JsonObject();
        }
    }
}
