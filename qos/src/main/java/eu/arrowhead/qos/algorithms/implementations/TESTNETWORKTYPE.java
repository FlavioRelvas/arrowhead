/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.qos.algorithms.implementations;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.qos.ResourceReservation;
import eu.arrowhead.common.messages.Event;
import eu.arrowhead.common.messages.PublishEvent;
import eu.arrowhead.common.messages.QoSVerifierResponse;
import eu.arrowhead.common.misc.CoreSystem;
import eu.arrowhead.common.misc.TypeSafeProperties;
import eu.arrowhead.qos.QoSMain;
import eu.arrowhead.qos.algorithms.IVerifierAlgorithm;
import eu.arrowhead.qos.algorithms.VerificationInfo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Flavio
 */
public class TESTNETWORKTYPE implements IVerifierAlgorithm {

    protected final TypeSafeProperties props = Utility.getProp("app.properties");

    //Checks for the other RejectMotivations need to be added
    @Override
    public QoSVerifierResponse verifyQoS(VerificationInfo info) {
        Map<String, String> consumerCapabilities = info.getConsumerDeviceCapabilities();
        Map<String, String> providerCapabilities = info.getProviderDeviceCapabilities();
        List<ResourceReservation> consumerReservations = info.getConsumerDeviceQoSReservations();
        List<ResourceReservation> providerReservations = info.getProviderDeviceQoSReservations();

        float bandwidthConsumer = BandwidthStringParser(consumerCapabilities.get("max_bandwidth"));
        float bandwidthProvider = BandwidthStringParser(providerCapabilities.get("max_bandwidth"));

        for (ResourceReservation r : consumerReservations) {
            if (r.getState().equalsIgnoreCase("up")) {
                bandwidthConsumer -= BandwidthStringParser(r.getQosParameters().get("bandwidth"));
            }
        }

        for (ResourceReservation r : providerReservations) {
            if (r.getState().equalsIgnoreCase("up")) {
                bandwidthConsumer -= BandwidthStringParser(r.getQosParameters().get("bandwidth"));
            }
        }

        float bandwidth = bandwidthConsumer > bandwidthProvider ? bandwidthProvider : bandwidthConsumer;

        Map<String, String> requestedQoS = info.getRequestedQoS();
        String requestedBandwidth = requestedQoS.get("bandwidth");
        float rBandwidth = BandwidthStringParser(requestedBandwidth);
        if (rBandwidth < bandwidth) {
            return new QoSVerifierResponse(true, null);
        } else {
            //triger Event
            String payload = "QoS parameters verification failed!";
            publishEvent(payload);
            return new QoSVerifierResponse(false, QoSVerifierResponse.RejectMotivationTypes.ALWAYS);
        }
    }

    private float BandwidthStringParser(String bandwidthString) {
        Pattern numbers = Pattern.compile("[0-9]+(\\.[0-9]*)?");
        Matcher m = numbers.matcher(bandwidthString);
        m.find();
        String number = m.group().trim();
        int t = m.end();
        float bandwidth;
        String unit = bandwidthString.substring(t).trim();
        switch (unit.toLowerCase().charAt(0)) {
            default:
                bandwidth = 0.0f;
                break;
            case 'm':
                bandwidth = Float.valueOf(number);
                break;
            case 'k':
                bandwidth = Float.valueOf(number) / 1024;
                break;
            case 'b':
                bandwidth = Float.valueOf(number) / 1024 / 1024;
                break;

        }
        return bandwidth;
    }

    private void publishEvent(String payload) {
        //Read in the Event Handler address related properties, create the full URL with the getUri() utility method
        String ehAddress = props.getProperty("eh_address", "0.0.0.0");
        int ehPort = props.getIntProperty("eh_insecure_port", 8454);
        String ehUri = Utility.getUri(ehAddress, ehPort, "eventhandler/publish", false, false);

        //Read in the fields needed to create the event
        String systemName = props.getProperty("insecure_system_name");
        String address = props.getProperty("address", "0.0.0.0");
        int insecurePort = props.getIntProperty("insecure_port", CoreSystem.QOS_MANAGER.getInsecurePort());
        int securePort = props.getIntProperty("secure_port", CoreSystem.QOS_MANAGER.getSecurePort());
        int usedPort = insecurePort;
        String type = props.getProperty("event_type");

        //Put together the event POJO and send the request to the Event Handler
        ArrowheadSystem source = new ArrowheadSystem(systemName, address, usedPort, null);
        Event event = new Event(type, payload, LocalDateTime.now(), null);
        System.out.println(Utility.toPrettyJson(null, event));
        PublishEvent eventPublishing = new PublishEvent(source, event, "");
        Utility.sendRequest(ehUri, "POST", eventPublishing);
        System.out.println("Event published to EH.");
    }
}
