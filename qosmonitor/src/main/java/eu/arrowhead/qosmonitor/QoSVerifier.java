/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.qosmonitor;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.qos.ResourceReservation;
import eu.arrowhead.common.messages.Event;
import eu.arrowhead.common.messages.PublishEvent;
import eu.arrowhead.common.misc.TypeSafeProperties;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Flavio
 */
public class QoSVerifier {

    protected static final TypeSafeProperties props = Utility.getProp("app.properties");

    private static final DatabaseManager dm = DatabaseManager.getInstance();

    public static boolean verify(ArrowheadSystem consumer, ArrowheadSystem provider, Map<String, String> parameters) {
        HashMap<String, Object> rm = new HashMap<>();

        rm.put("consumer", consumer);
        rm.put("provider", provider);
        System.out.println(Utility.toPrettyJson(null, rm));
        List<ResourceReservation> reserved = dm.getAll(ResourceReservation.class, rm);

        for (ResourceReservation rr : reserved) {
            if (rr.getState().equalsIgnoreCase("UP")) {
                for (String key : rr.getQosParameters().keySet()) {
                    switch (key) {
                        case "bandwidth":
                            float br = bandwidthStringParser(rr.getQosParameters().get(key));
                            float bo = bandwidthStringParser(parameters.get(key));
                            if (br < bo) {
                                String payload = "SLA breach\nConsumer:{ "
                                        + "\n\tname: " + consumer.getSystemName() + " "
                                        + "\n\taddress: " + consumer.getAddress() + " "
                                        + "\n\tport: " + consumer.getPort() + " \n}"
                                        + "\nProvider: {"
                                        + "\n\tname: " + provider.getSystemName() + " "
                                        + "\n\taddress: " + provider.getAddress() + " "
                                        + "\n\tport: " + provider.getPort() + " \n}"
                                        + "\nRequestedParameters: {"
                                        + "\n\tbandwidth: " + br + "\n}"
                                        + "\nObservedParameters: {"
                                        + "\n\tbandwidth: " + bo + "\n}";
                                publishEvent(payload);
                                return false;
                            }
                            break;
                        case "packet loss":
                            break;
                        case "response time":
                            break;
                    }
                }
            }
        }
        return true;
    }

    //refactor to be useable in every application
    private static float bandwidthStringParser(String bandwidthString) {
        bandwidthString = bandwidthString.replace(",", ".");
        Pattern numbers = Pattern.compile("[0-9]+([\\.][0-9]*)?");
        Matcher m = numbers.matcher(bandwidthString);
        m.find();
        String number = m.group();
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

    private static void publishEvent(String payload) {
        //Read in the Event Handler address related properties, create the full URL with the getUri() utility method
        String ehAddress = props.getProperty("eh_address", "0.0.0.0");
        int ehPort = props.getIntProperty("eh_insecure_port", 8454);
        String ehUri = Utility.getUri(ehAddress, ehPort, "eventhandler/publish", false, false);

        //Read in the fields needed to create the event
        String systemName = props.getProperty("insecure_system_name");
        String address = props.getProperty("address", "0.0.0.0");
        int insecurePort = props.getIntProperty("insecure_port", 8456);
        int securePort = props.getIntProperty("secure_port", 8457);
        int usedPort = insecurePort;
        String type = props.getProperty("event_type");

        //Put together the event POJO and send the request to the Event Handler
        ArrowheadSystem source = new ArrowheadSystem(systemName, address, usedPort, null);
        Event event = new Event(type, payload, LocalDateTime.now(), null);
        System.out.println(Utility.toPrettyJson(null, event));
        PublishEvent eventPublishing = new PublishEvent(source, event, "monitor/feedback");
        Utility.sendRequest(ehUri, "POST", eventPublishing);
        System.out.println("Event published to EH.");
    }
}
