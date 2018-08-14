/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.qos.algorithms.implementations;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.qos.DeployedSystem;
import eu.arrowhead.common.database.qos.Network;
import eu.arrowhead.common.messages.QoSVerifierResponse;
import eu.arrowhead.qos.algorithms.IVerifierAlgorithm;
import eu.arrowhead.qos.algorithms.VerificationInfo;
import eu.arrowhead.qos.algorithms.VerificationResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Flavio
 */
public class TESTNETWORKTYPE implements IVerifierAlgorithm {

    //Checks for the other RejectMotivations need to be added
    
    
    @Override
    public QoSVerifierResponse verifyQoS(VerificationInfo info) {
        Map<String,String> consumerCapabilities = info.getConsumerDeviceCapabilities();
        Map<String,String> providerCapabilities = info.getProviderDeviceCapabilities();
        
        float bandwidthConsumer = BandwidthStringParser(consumerCapabilities.get("max_bandwidth"));
        float bandwidthProvider = BandwidthStringParser(providerCapabilities.get("max_bandwidth"));
        float bandwidth = bandwidthConsumer > bandwidthProvider ? bandwidthProvider : bandwidthConsumer;
        Map<String, String> requestedQoS = info.getRequestedQoS();
        String requestedBandwidth = requestedQoS.get("bandwidth");
        float rBandwidth = BandwidthStringParser(requestedBandwidth);
        if (rBandwidth < bandwidth) {
            return new QoSVerifierResponse(true, null);
        } else {
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
}
