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
 * @author Renato Ayres
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
//    /**
//     * Returns an initialized instance of the MongoDatabaseManager class.
//     *
//     * @return MongoDatabaseManager static instance
//     */
//    public MongoDatabaseManager getDatabaseManager() {
//        if (database == null) {
//            database = new MongoDatabaseManager();
//        }
//        return database;
//    }
//    /**
//     * Returns an initialized instance of the MongoDatabaseManager class.
//     *
//     * @return MongoDatabaseManager static instance
//     */
//    public MongoDatabaseManager getDatabaseManager() {
//        if (database == null) {
//            database = new MongoDatabaseManager();
//        }
//        return database;
//    }
//    /**
//     * Returns an initialized instance of the MongoDatabaseManager class.
//     *
//     * @return MongoDatabaseManager static instance
//     */
//    public MongoDatabaseManager getDatabaseManager() {
//        if (database == null) {
//            database = new MongoDatabaseManager();
//        }
//        return database;
//    }

    public void startService() {
        //Register QoSMonitor service in service registry
        //Service Registry
        Register register = new Register();
        register.registerAll();

        // [PT] Starting MongoDB, loading EventProducer configurations and registering in EventHandler
//        EventProducerConfig.loadConfigurations();
//        new ProducerRegistry().registerAsProducer();
//
//        MongoDatabaseManager.getInstance().startManager();
    }

    /**
     * Intermediates between message and monitor type.
     *
     * @param message EventMessage message
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     *
     * public void sendEvent(EventMessage message) throws
     * InstantiationException, IllegalAccessException {
     *
     * if (message.getParameters().isEmpty()) { //throw new
     * NoMonitorParametersException("No parameters found in service error
     * message!"); }
     *
     * IProtocol monitor = null; try { monitor =
     * getMonitorClass(message.getProtocol()); } catch (ClassNotFoundException
     * ex) { String excMessage = "Type " + message.getProtocol() + " not found.
     * Make " + "sure you have the right monitor type for your " + "situation
     * and that it's available in this version " + "and/or not misspelled.";
     * //log.error(excMessage); // throw new
     * InvalidMonitorTypeException(excMessage); }
     *
     * Event event = monitor.createEvent(message);
     *
     * // EventProducer producer = new EventProducer(event); //
     * producer.publishEvent(); }
     */
    public void newLog(AddLogForm message) {
        Gson gson = new Gson();
        if (message.getParameters().isEmpty()) {
            System.out.println("No parameters");
            //throw new NoMonitorParametersException("No monitor parameters found!");
        }

        dm.save(message);
        QoSMonitorMain.systemsMap.put(QoSMonitorMain.mapIndex, message);
        QoSMonitorMain.mapIndex = QoSMonitorMain.mapIndex + 1;

//        SLAVerification verification = new SLAVerification(monitor, rule, log);
//        EXEC.execute(verification);
        //MongoDatabaseManager.getInstance().newLog(message, System.currentTimeMillis());
        //QoSMonitorService.log.info(String.format("Executing SLAVerification [SEPARATE THREAD]"));
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
