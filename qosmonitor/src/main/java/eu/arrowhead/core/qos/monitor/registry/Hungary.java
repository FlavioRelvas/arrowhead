/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.core.qos.monitor.registry;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;

/**
 *
 * @author ID0084D
 */
public class Hungary implements ServiceRegister {

    private Properties props;
    private static final Logger LOG = Logger.getLogger(Hungary.class.getName());

    /**
     * Gets the properties file named 'registry.properties'.
     *
     * @return the Properties from properties file 'registry.properties'
     */
    private synchronized Properties getProps() {
        try {
            if (props == null) {
                props = new Properties();
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("hungary.properties");
                if (inputStream != null) {
                    props.load(inputStream);
                    inputStream.close();
                } else {
                    throw new FileNotFoundException("Properties file 'hungary.properties' not found in the classpath");
                }
            }
        } catch (FileNotFoundException ex) {
            LOG.error(ex.getMessage());
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        return props;
    }

    @Override
    public boolean registerQoSMonitorService() {

        ServiceRegistryEntry entry = createServiceRegistryEntry();

        Properties temp = getProps();

        String registryURI = temp.getProperty("serviceregistry.uri");
       // Utility.setServiceRegistryUri(registryURI);
        LOG.info("Request payload: " + Entity.json(entry));

        Response response = Utility.sendRequest(UriBuilder.fromUri(registryURI).path("serviceregistry").path("register").build().toString(), "POST", entry);
//        try {
//            Utility.sendRequest(UriBuilder.fromUri(registryURI).path("register").build().toString(), "POST", entry);
//            return true;
//        } catch (ArrowheadException e) {
//            if (e.getExceptionType() == ExceptionType.DUPLICATE_ENTRY) {
//                Utility.sendRequest(UriBuilder.fromUri(registryURI).path("remove").build().toString(), "PUT", entry);
//                Utility.sendRequest(UriBuilder.fromUri(registryURI).path("register").build().toString(), "POST", entry);
//            } else {
//                throw new ArrowheadException("QoS monitor service registration failed.", e);
//            }
//        }
        return false;

    }

    @Override
    public boolean unregisterQoSMonitorService() {
        ServiceRegistryEntry entry = createServiceRegistryEntry();

        Properties temp = getProps();

        String registryURI = temp.getProperty("serviceregistry.uri");

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(registryURI);

        String serviceGroup = temp.getProperty("monitor.service.group");
        String serviceName = temp.getProperty("monitor.service.name");
        String interfaces = temp.getProperty("monitor.service.interfaces");

        Response response = target
                .path(serviceGroup)
                .path(serviceName)
                .path(interfaces)
                .request()
                .header("Content-Type", "application/json")
                .put(Entity.json(entry));

        int statusCode = response.getStatus();
        LOG.info("ServiceRegistry response: " + statusCode);

        client.close();

        return statusCode > 199 && statusCode < 300;
    }

    private ServiceRegistryEntry createServiceRegistryEntry() {

        ArrowheadService service = createArroheadMonitorService();
        ArrowheadSystem provider = createArrowheadMonitorSystem();

        return new ServiceRegistryEntry(service, provider, "monitor");
    }

    private ArrowheadSystem createArrowheadMonitorSystem() {
        Properties temp = getProps();

        String systemName = temp.getProperty("monitor.system.name");

        String address;
        try {
            address = Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            LOG.warn("Not able to get local host from system. Using default value in hungary.properties file");
            address = temp.getProperty("monitor.system.address");
        }

        String tPort = temp.getProperty("monitor.system.port");
        int port = Integer.parseInt(tPort);
        String authenticationInfo = temp.getProperty("monitor.system.authenticationInfo");

        return new ArrowheadSystem(systemName, address, port, null);
    }

    private ArrowheadService createArroheadMonitorService() {
        Properties temp = getProps();

        Map<String, String> serviceMetadata = new HashMap<String, String>();

        String[] keys = temp.getProperty("monitor.service.metadata.key").split(",");
        Queue<String> values = new ArrayDeque<String>(Arrays.asList(temp.getProperty("monitor.service.metadata.value").split(",")));

        for (String key : keys) {
            serviceMetadata.put(key, values.remove());
        }

        String serviceName = "QoSMonitor";
        ArrowheadService service = new ArrowheadService(serviceName, Collections.singletonList("json"), serviceMetadata);
        return service;

    }
}
