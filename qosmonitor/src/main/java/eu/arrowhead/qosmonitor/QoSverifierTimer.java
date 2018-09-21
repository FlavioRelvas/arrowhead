/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.qosmonitor;

import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.qos.AddLogForm;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

/**
 *
 * @author root
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
