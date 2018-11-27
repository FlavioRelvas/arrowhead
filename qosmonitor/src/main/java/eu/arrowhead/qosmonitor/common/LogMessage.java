/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This work was partially supported by National Funds through FCT/MCTES (Portuguese Foundation
 * for Science and Technology), within the CISTER Research Unit (CEC/04234) and also by
 * Grant nr. 737459 Call H2020-ECSEL-2016-2-IA-two-stage 
 * ISEP/CISTER, Polytechnic Institute of Porto.
 * Luis Lino Ferreira (llf@isep.ipp.pt), Flávio Relvas (flaviofrelvas@gmail.com),
 * Michele Albano (mialb@isep.ipp.pt), Rafael Teles Da Rocha (rtdrh@isep.ipp.pt)
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
