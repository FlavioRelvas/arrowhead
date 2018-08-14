/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.core.orchestrator.support;

import eu.arrowhead.common.database.ArrowheadSystem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Flavio
 */
public class QoSRequestForm {

    private ArrowheadSystem consumer;
    private List<ArrowheadSystem> providers;
    private Map<String, String> requestedQoS = new HashMap<>();

    private QoSRequestForm(Builder builder) {
        consumer = builder.clientSystem;
        providers = builder.providerSystems;
        requestedQoS = builder.requestedQoS;
    }

    public ArrowheadSystem getRequesterSystem() {
        return consumer;
    }

    public void setRequesterSystem(ArrowheadSystem requesterSystem) {
        this.consumer = requesterSystem;
    }

    public List getProviderSystems() {
        return providers;
    }

    public Map<String, String> getRequestedQoS() {
        return requestedQoS;
    }

    public void setRequestedQoS(Map<String, String> requestedQoS) {
        this.requestedQoS = requestedQoS;
    }

    public static class Builder {

        // Required parameters
        private ArrowheadSystem clientSystem;
        private List<ArrowheadSystem> providerSystems;
        private Map<String, String> requestedQoS = new HashMap<>();

        public Builder(ArrowheadSystem clientSystem, List<ArrowheadSystem> providerSystems, Map<String, String> qosParameters) {
            this.clientSystem = clientSystem;
            this.providerSystems = providerSystems;
            this.requestedQoS = qosParameters;
        }

        public QoSRequestForm build() {
            return new QoSRequestForm(this);
        }
    }
}
