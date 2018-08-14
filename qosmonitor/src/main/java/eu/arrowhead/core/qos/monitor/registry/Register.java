/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.core.qos.monitor.registry;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * Class for service registry related operations, like register and unregister.
 * Uses the serviceregistry.properties file.
 *
 * @author 1120681@isep.ipp.pt - Renato Ayres
 */
public class Register {

    private static final Logger LOG = Logger.getLogger(Register.class.getName());
    private final String MONITOR_REGISTRY_PACKAGE = "eu.arrowhead.core.qos.monitor.registry.";
    private static final List<String> REGISTERED = new ArrayList<>();

    /**
     * Creates a new instance of Register
     */
    public Register() {
    }

    /**
     * Registers the QoSMonitor in all the ServiceRegistry instances present in
     * the serviceregistry.properties file. Saves information of the successful
     * registrations
     */
    public void registerAll() {
        LOG.info(String.format("Entered the registerAll method"));
        //Register QoSMonitor service in service registry
        List<String> registries = getServiceRegistry();
        System.out.println("REGISTRIES " + registries.size());

        for (String registry : registries) {
            try {
                ServiceRegister register = getRegistryClass(registry);
                if (register.registerQoSMonitorService()) {
                    REGISTERED.add(registry);
                    LOG.info(String.format("Register in {0} successful!", register.getClass().getName()));
                } else {
                    LOG.warn(String.format("Register in {0} unsuccessful!", register.getClass().getName()));
                }
            } catch (ClassNotFoundException ex) {
                String excMessage = "Not registered in registry " + registry + ". "
                        + "Registry class " + registry + " not found. Make "
                        + "sure you have the right registry class for your "
                        + "situation and that it's available in this version "
                        + "and/or not misspelled.";
                LOG.error(excMessage);
//                throw new RuntimeException(excMessage);
            } catch (InstantiationException | IllegalAccessException ex) {
                LOG.error(ex.getMessage());
            }
        }
        System.out.println("REGISTERED: " + REGISTERED.size());
    }

    /**
     * Unregisters the QoSMonitor in all the ServiceRegistry instances
     * previously saved by the {@link #registerAll() registerAll} method
     */
    public void unregisterAll() {
        LOG.info(String.format("Entered the unregisterAll method"));

        List<String> successful = new ArrayList<>();

        //TODO unregister from EventHandler
        //Unregister from ServiceRegistry
        for (String registry : REGISTERED) {
            try {
                ServiceRegister register = getRegistryClass(registry);
                if (register.unregisterQoSMonitorService()) {
                    successful.add(registry);
                    LOG.info(String.format("Unregister in {0} successful!", register.getClass().getName()));
                } else {
                    LOG.warn(String.format("Unregister in {0} unsuccessful!", register.getClass().getName()));
                }
            } catch (ClassNotFoundException ex) {
                String excMessage = "Registry class " + registry + " not found. Make "
                        + "sure you have the right registry class for your "
                        + "situation and that it's available in this version "
                        + "and/or not misspelled.";
                LOG.error(excMessage);
                throw new RuntimeException(excMessage);
            } catch (InstantiationException | IllegalAccessException ex) {
                LOG.error(ex.getMessage());
            }
        }
        REGISTERED.removeAll(successful);
    }

    private List<String> getServiceRegistry() {
        LOG.info(String.format("Entered the getServiceRegistry method"));
        Properties props = null;
        String[] registries;
        try {
            props = new Properties();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("serviceregistry.properties");
            if (inputStream != null) {
                LOG.info(String.format("Found serviceregistry.properties file"));
                props.load(inputStream);
                inputStream.close();
            } else {
                String exMsg = "Properties file 'serviceregistry.properties' not found in the classpath";
                LOG.error(exMsg);
                throw new FileNotFoundException(exMsg);
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
        registries = props.getProperty("registry.option").trim().split(",");
        if (registries.length == 0) {
            String exMsg = "No ServiceRegistry values found in registry.option of serviceregistry.properties file.";
            LOG.error(exMsg);
            throw new RuntimeException(exMsg);
        }
        return Arrays.asList(registries);
    }

    /**
     * Returns an initialized instance of class, finding it by it's name.
     *
     * @param name name of the class to instantiate
     * @return a Monitor implementation
     * @throws ClassNotFoundException thrown when trying to initialize a class
     * that cannot be found. This may be due to the type in the message being
     * wrong or mistyped
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private ServiceRegister getRegistryClass(String name)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class<?> cl;

        cl = Class.forName(MONITOR_REGISTRY_PACKAGE + name);

        ServiceRegister monitor = (ServiceRegister) cl.newInstance();

        return monitor;
    }

}
