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

import com.google.gson.Gson;
import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.core.qos.monitor.registry.Register;
import eu.arrowhead.common.database.qos.AddLogForm;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is the QoSMonitor Service class. It takes care of all the aspects of the
 * QoSMonitor Service.
 *
 * @author Renato Ayres & Flávio
 */
public class QoSMonitorService {

    private static final String MONITOR_TYPE_PACKAGE = "eu.arrowhead.core.qos.monitor.protocol.";
    private static final ExecutorService EXEC = Executors.newCachedThreadPool();

    private final DatabaseManager dm = DatabaseManager.getInstance();

    public static boolean SHOW_GRAPHS = true;

    //FIXME only used in startService
    private static final List<String> REGISTERED = new ArrayList();
    //FIXME only used in startService
    private final String MONITOR_REGISTRY_PACKAGE = "eu.arrowhead.core.qos.monitor.register.";
    private static Properties prop = getProp();

    /**
     * A new QoSMonitorService instance with a initialized MongoDatabaseManager
     * instance.
     */
    public QoSMonitorService() {
    }

    public void startService() {
        //Register QoSMonitor service in service registry
        //Service Registry
        Register register = new Register();
        register.registerAll();
    }

    public void newLog(AddLogForm message) {
        Gson gson = new Gson();
        if (message.getParameters().isEmpty()) {
            System.out.println("No parameters");
            //throw new NoMonitorParametersException("No monitor parameters found!");
        }

        dm.save(message);
        QoSMonitorMain.systemsMap.put(QoSMonitorMain.mapIndex, message);
        QoSMonitorMain.mapIndex = QoSMonitorMain.mapIndex + 1;

    }

    private static synchronized Properties getProp() {
        try {
            if (prop == null) {
                prop = new Properties();
                File file = new File("config" + File.separator + "app.properties");
                FileInputStream inputStream = new FileInputStream(file);
                prop.load(inputStream);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return prop;
    }

}
