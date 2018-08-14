/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.qosmonitortest;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.qos.ResourceReservation;
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
}
