/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.qosmonitor.common;

import eu.arrowhead.common.database.ArrowheadSystem;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Flavio
 */

public class LogMessage {

    private ArrowheadSystem consumer;
    private ArrowheadSystem provider;
    private Map<String, String> parameters; //the QoS parameters

    public LogMessage() {
    }

    public LogMessage(ArrowheadSystem provider, ArrowheadSystem consumer, Map<String, String> parameters) {
        this.provider = provider;
        this.consumer = consumer;
        this.parameters = parameters;
    }

    public ArrowheadSystem getConsumer() {
        return consumer;
    }

    public void setConsumer(ArrowheadSystem consumer) {
        this.consumer = consumer;
    }

    public ArrowheadSystem getProvider() {
        return provider;
    }

    public void setProvider(ArrowheadSystem provider) {
        this.provider = provider;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

}
