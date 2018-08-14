package eu.arrowhead.common.messages;


import eu.arrowhead.common.database.ArrowheadSystem;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EventMessage {

    private String protocol;
    private ArrowheadSystem system;
    private Map<String, String> parameters;
    private String errorMessage;

    public EventMessage() {
        super();
        // TODO Auto-generated constructor stub
    }

    public EventMessage(String protocol, ArrowheadSystem system, Map<String, String> parameters, String errorMessage) {
        super();
        this.protocol = protocol;
        this.system = system;
        this.parameters = parameters;
        this.errorMessage = errorMessage;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public ArrowheadSystem getSystem() {
        return system;
    }

    public void setSystem(ArrowheadSystem system) {
        this.system = system;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
