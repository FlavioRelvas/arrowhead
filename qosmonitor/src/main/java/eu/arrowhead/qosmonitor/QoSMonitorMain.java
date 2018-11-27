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

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.database.qos.AddLogForm;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.ExceptionType;
import eu.arrowhead.common.security.SecurityUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

/**
 *
 * @author Flavio
 */
public class QoSMonitorMain {

    public static boolean DEBUG_MODE;
    public static PublicKey authorizationKey;
    public static PrivateKey privateKey;

    private static String SR_BASE_URI = getProp().getProperty("sr_base_uri", "http://localhost:8442/serviceregistry");
    private static HttpServer server;
    private static HttpServer secureServer;
    private static String MONITOR_PUBLIC_KEY;
    private static Properties prop;

    private static final String BASE_URI = getProp().getProperty("base_uri", "http://localhost:8454/");
    private static final String BASE_URI_SECURED = getProp().getProperty("base_uri_secured", "https://localhost:8455/");
    private static final Logger log = Logger.getLogger(QoSMonitorMain.class.getName());

    public static Map<String, LocalDateTime> m = new HashMap<>();
    public static Map<Integer, AddLogForm> systemsMap = new HashMap<>();
    public static Integer mapIndex = new Integer(0);
    protected static final int TIMER_INTERVAL = Integer.parseInt(getProp().getProperty("timer_interval", "15"));

    public static void main(String[] args) throws IOException {
        PropertyConfigurator.configure("config" + File.separator + "log4j.properties");
        System.out.println("Working directory: " + System.getProperty("user.dir"));

        if (!SR_BASE_URI.contains("serviceregistry")) {
            SR_BASE_URI = UriBuilder.fromUri(SR_BASE_URI).path("serviceregistry").build().toString();
        }

        boolean daemon = false;
        boolean serverModeSet = false;
        for (int i = 0; i < args.length; ++i) {
            switch (args[i]) {
                case "-daemon":
                    daemon = true;
                    System.out.println("Starting server as daemon!");
                    break;
                case "-d":
                    DEBUG_MODE = true;
                    System.out.println("Starting server in debug mode!");
                    break;
                case "-m":
                    serverModeSet = true;
                    ++i;
                    switch (args[i]) {
                        case "insecure":
                            server = startServer();
                            break;
                        case "secure":
                            secureServer = startSecureServer();
                            break;
                        case "both":
                            server = startServer();
                            secureServer = startSecureServer();
                            break;
                        default:
                            throw new AssertionError("Unknown server mode: " + args[i]);
                    }
            }
        }
        if (!serverModeSet) {
            server = startServer();
        }

        List<ServiceRegistryEntry> registeredEntries = registerToServiceRegistry();

        TimerTask timerTask = new QoSverifierTimer();
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 0, TIMER_INTERVAL * 1000);

        if (daemon) {
            System.out.println("In daemon mode, process will terminate for TERM signal...");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Received TERM signal, shutting down...");
                shutdown(registeredEntries);
            }));
        } else {
            System.out.println("Type \"stop\" to shutdown Authorization Server(s)...");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String input = "";
            while (!input.equals("stop")) {
                input = br.readLine();
            }
            br.close();
            shutdown(registeredEntries);
        }
    }

    private static HttpServer startServer() throws IOException {
        URI uri = UriBuilder.fromUri(BASE_URI).build();
        final ResourceConfig config = new ResourceConfig();
        config.registerClasses(QoSMonitorResource.class);
        config.packages("eu.arrowhead.qosmonitortest.common.filter");

        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config);
        server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
        server.start();
        System.out.println("Insecure server launched...");
        return server;
    }

    private static HttpServer startSecureServer() throws IOException {

        final ResourceConfig config = new ResourceConfig();
        config.registerClasses(QoSMonitorResource.class);
        config.packages("eu.arrowhead.qosmonitortest.common.filter", "eu.arrowhead.qosmonitortest.common.security");

        String keystorePath = getProp().getProperty("keystore");
        String keystorePass = getProp().getProperty("keystorepass");
        String keyPass = getProp().getProperty("keypass");
        String truststorePath = getProp().getProperty("truststore");
        String truststorePass = getProp().getProperty("truststorepass");

        SSLContextConfigurator sslCon = new SSLContextConfigurator();
        sslCon.setKeyStoreFile(keystorePath);
        sslCon.setKeyStorePass(keystorePass);
        sslCon.setKeyPass(keyPass);
        sslCon.setTrustStoreFile(truststorePath);
        sslCon.setTrustStorePass(truststorePass);
        if (!sslCon.validateConfiguration(true)) {
            throw new AuthException("SSL Context is not valid, check the certificate files or app.properties!");
        }

        SSLContext sslContext = sslCon.createSSLContext();
        Utility.setSSLContext(sslContext);

        // Getting certificate keys
        KeyStore keyStore = SecurityUtils.loadKeyStore(keystorePath, keystorePass);
        privateKey = SecurityUtils.getPrivateKey(keyStore, keystorePass);
        X509Certificate serverCert = SecurityUtils.getFirstCertFromKeyStore(keyStore);
        MONITOR_PUBLIC_KEY = Base64.getEncoder().encodeToString(serverCert.getPublicKey().getEncoded());
        System.out.println("My certificate PublicKey in Base64: " + MONITOR_PUBLIC_KEY);
        String serverCN = SecurityUtils.getCertCNFromSubject(serverCert.getSubjectDN().getName());
        System.out.println("My certificate CN: " + serverCN);
        config.property("server_common_name", serverCN);

        if (!SecurityUtils.isKeyStoreCNArrowheadValid(serverCN)) {
            log.fatal("Server CN is not compliant with the Arrowhead cert structure");
            throw new AuthException(
                    "Server CN ( " + serverCN + ") is not compliant with the Arrowhead cert structure, since it does not have 5 parts, or does not "
                    + "end with arrowhead.eu.", Response.Status.UNAUTHORIZED.getStatusCode());
        }
        log.info("Certificate of the secure server: " + serverCN);
        config.property("server_common_name", serverCN);

        URI uri = UriBuilder.fromUri(BASE_URI_SECURED).build();
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config, true, new SSLEngineConfigurator(sslCon).setClientMode(false).setNeedClientAuth(true));
        server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
        server.start();
        System.out.println("Secure server launched...");
        return server;
    }

    private static void shutdown(List<ServiceRegistryEntry> registeredEntries) {
        unregisterFromServiceRegistry(registeredEntries);
        if (server != null) {
            server.shutdownNow();
        }
        if (secureServer != null) {
            secureServer.shutdownNow();
        }

        System.out.println("QoSMonitor Server(s) stopped.");
    }

    private static List<ServiceRegistryEntry> registerToServiceRegistry() {
        List<ServiceRegistryEntry> entries = new ArrayList<>();

        // create the URI for the request
        String registerUri = UriBuilder.fromPath(SR_BASE_URI).path("register").toString();

        // create the metadata HashMap for the service
        Map<String, String> metadata = new HashMap<>();
        //metadata.put("unit", "watt");

        // create the ArrowheadService object
        ArrowheadService service = new ArrowheadService("QoSMonitor", Collections.singletonList("JSON"), metadata);

        // objects specific to insecure mode
        if (server != null) {
            URI baseUri;
            try {
                baseUri = new URI(BASE_URI);
            } catch (URISyntaxException e) {
                throw new RuntimeException("Parsing the BASE_URI resulted in an error.", e);
            }
            // create the ArrowheadSystem object
            ArrowheadSystem monitor = new ArrowheadSystem("QoSMonitor", baseUri.getHost(), baseUri.getPort(), null);
            // create the final request payload
            ServiceRegistryEntry entry = new ServiceRegistryEntry(service, monitor, "monitor");
            System.out.println("Request payload: " + Utility.toPrettyJson(null, entry));

            try {
                Utility.sendRequest(registerUri, "POST", entry);
            } catch (ArrowheadException e) {
                if (e.getExceptionType() == ExceptionType.DUPLICATE_ENTRY) {
                    System.out.println("Received DuplicateEntryException from SR, sending delete request and then registering again.");
                    unregisterFromServiceRegistry(Collections.singletonList(entry));
                    Utility.sendRequest(registerUri, "POST", entry);
                }
            }
            System.out.println("Registering insecure service is successful!");
            entries.add(entry);
        }

        // objects specific to secure mode
        if (secureServer != null) {
            // adding metadata indicating the security choice of the monitor
            metadata.put("security", "token");

            URI baseUri;
            try {
                baseUri = new URI(BASE_URI_SECURED);
            } catch (URISyntaxException e) {
                throw new RuntimeException("Parsing the BASE_URI_SECURED resulted in an error.", e);
            }
            // create the ArrowheadSystem object
            ArrowheadSystem provider = new ArrowheadSystem("SecurePowerSensor", baseUri.getHost(), baseUri.getPort(), MONITOR_PUBLIC_KEY);
            // create the final request payload
            ServiceRegistryEntry entry = new ServiceRegistryEntry(service, provider, "power");
            System.out.println("Request payload: " + Utility.toPrettyJson(null, entry));
            try {
                Utility.sendRequest(registerUri, "POST", entry);
            } catch (ArrowheadException e) {
                if (e.getExceptionType() == ExceptionType.DUPLICATE_ENTRY) {
                    System.out.println("Received DuplicateEntryException from SR, sending delete request and then registering again.");
                    unregisterFromServiceRegistry(Collections.singletonList(entry));
                    Utility.sendRequest(registerUri, "POST", entry);
                } else {
                    throw e;
                }
            }
            System.out.println("Registering secure service is successful!");
            entries.add(entry);
        }

        return entries;
    }

    private static void unregisterFromServiceRegistry(List<ServiceRegistryEntry> registeredEntries) {
        // create the URI for the request
        String removeUri = UriBuilder.fromPath(SR_BASE_URI).path("remove").toString();
        // remove every service we registered (2 at max)
        for (ServiceRegistryEntry entry : registeredEntries) {
            Utility.sendRequest(removeUri, "PUT", entry);
        }

        System.out.println("Removing service(s) is successful!");
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
