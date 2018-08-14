/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.core.qos.monitor.event;

import eu.arrowhead.core.qos.monitor.event.model.Event;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;

/**
 *
 * @author Renato Ayres
 */
public class EventProducer {

    private final Client client;
    private final WebTarget target;
    private final Event event;
    private static String producer;
    private static final Logger LOG = Logger.getLogger(EventProducer.class.getName());

    public EventProducer(Event event) {
        client = ClientBuilder.newClient();
        target = client.target(EventProducerConfig.getServiceURI());
        this.event = event;
    }

//    /**
//     * Gets the properties file named 'monitor.properties'.
//     *
//     * @return the Properties from properties file 'monitor.properties'
//     */
//    private synchronized Properties getProps() {
//        Properties props = null;
//        try {
//            props = new Properties();
//            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("monitor.properties");
//            if (inputStream != null) {
//                props.load(inputStream);
//                inputStream.close();
//            } else {
//                throw new FileNotFoundException("Properties file 'eventhandler.properties' not found in the classpath");
//            }
//        } catch (FileNotFoundException ex) {
//            LOG.log(Level.SEVERE, ex.getMessage());
//        } catch (IOException ex) {
//            LOG.log(Level.SEVERE, ex.getMessage());
//        }
//        return props;
//    }
    public Client getClient() {
        return client;
    }

    public WebTarget getTarget() {
        return target;
    }

    public Event getEvent() {
        return event;
    }

    public static String getProducer() {
        return producer;
    }

    public static void setProducer(String producer) {
        EventProducer.producer = producer;
    }

    public int publishEvent() {
        LOG.info(String.format("Sending event to EventHandler. Service URI: {0}\nPublish URI: {1}", new Object[]{target.getUri(), EventProducerConfig.getServicePublishEventPath()}));

        Response response = getTarget()
                .path(EventProducerConfig.getServicePublishEventPath())
                .path(getProducer())
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(event));

        int statusCode = response.getStatus();

        client.close();

        LOG.info(String.format("Response status received from EventHandler: {0}", statusCode));

        return statusCode;
    }
}
