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
package eu.arrowhead.qosmonitor;

import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.qos.AddLogForm;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

/**
 *
 * @author Flavio
 */
public class QoSverifierTimer extends TimerTask {

    private float time = QoSMonitorMain.TIMER_INTERVAL;

    @Override
    public void run() {
        ArrowheadSystem consumer;
        ArrowheadSystem provider;
        Map<String, String> parameters = new HashMap<>();
        Map<String, Integer> parametersParsed = new HashMap<>();

        Map<Integer, AddLogForm> p = new HashMap(QoSMonitorMain.systemsMap);
        QoSMonitorMain.systemsMap.clear();

        for (AddLogForm m : p.values()) {
            consumer = m.getConsumer();
            provider = m.getProvider();
            parameters = m.getParameters();

            if (!parametersParsed.containsKey(consumer.getSystemName() + "-" + provider.getSystemName())) {
                parametersParsed.put(consumer.getSystemName() + "-" + provider.getSystemName(), Integer.parseInt(parameters.get("bandwidth")));
            } else {
                parametersParsed.put(consumer.getSystemName() + "-" + provider.getSystemName(), parametersParsed.get(consumer.getSystemName() + "-"
                        + provider.getSystemName()) + Integer.parseInt(parameters.get("bandwidth")));
            }

        }

        for (AddLogForm m : p.values()) {
            String s = m.getConsumer().getSystemName() + "-" + m.getProvider().getSystemName();
            if (parametersParsed.containsKey(s)) {
                Map<String, String> map = new HashMap<>();
                map.put("bandwidth", String.format("%.2fb/s", (float) parametersParsed.get(s) / time));
                if (!QoSVerifier.verify(m.getConsumer(), m.getProvider(), map)) {
                    System.out.println("Requested QoS parameters not being guaranteed");
                }
            }
        }

    }

}
