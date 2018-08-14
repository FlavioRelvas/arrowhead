package eu.arrowhead.common.messages;


import eu.arrowhead.common.database.ArrowheadSystem;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Message used to add a new monitor log.
 *
 * @author Renato Ayres
 */
@XmlRootElement
public class AddMonitorLog {

    private String protocol;
    private ArrowheadSystem provider;
    private ArrowheadSystem consumer;
    private Map<String, String> parameters;
    private Long timestamp;

    /**
     * Creates a new instance with no parameters initialized.
     */
    public AddMonitorLog() {
    }

    /**
     * Creates a new instance with the given monitor protocol, service provider,
 service consumer, monitor parameters, and monitor timestamp.
     *
     * @param protocol the monitor protocol
     * @param provider the service provider
     * @param consumer the service consumer
     * @param parameters the monitor parameters. It works by getting the value
     * of the parameter (key) e.g. key=bandwidth, value=100
     * @param timestamp the monitor timestamp
     */
    public AddMonitorLog(String protocol, ArrowheadSystem provider,
            ArrowheadSystem consumer, Map<String, String> parameters, Long timestamp) {
        this.protocol = protocol;
        this.provider = provider;
        this.consumer = consumer;
        this.parameters = parameters;
        this.timestamp = timestamp;
    }

    /**
     * Gets the monitor protocol
     *
     * @return the monitor protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets the monitor protocol
     *
     * @param protocol the monitor protocol
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Gets the service provider
     *
     * @return the service provider
     */
    public ArrowheadSystem getProvider() {
        return provider;
    }

    /**
     * Sets the service provider
     *
     * @param provider the service provider
     */
    public void setProvider(ArrowheadSystem provider) {
        this.provider = provider;
    }

    /**
     * Gets the service consumer
     *
     * @return the service consumer
     */
    public ArrowheadSystem getConsumer() {
        return consumer;
    }

    /**
     * Sets the service consumer
     *
     * @param consumer the service consumer
     */
    public void setConsumer(ArrowheadSystem consumer) {
        this.consumer = consumer;
    }

    /**
     * Gets the monitor parameters
     *
     * @return the monitor parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Sets the monitor parameters.
     *
     * @param parameters the monitor parameters
     */
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * Gets the monitor timestamp
     *
     * @return the monitor timestamp
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the monitor timestamp
     *
     * @param timestamp the monitor timestamp
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}