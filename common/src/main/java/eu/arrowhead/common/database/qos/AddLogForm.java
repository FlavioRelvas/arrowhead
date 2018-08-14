/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.common.database.qos;

import eu.arrowhead.common.database.ArrowheadSystem;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

/**
 *
 * @author Flavio
 */
@Entity
@Table(name = "monitor_log")
public class AddLogForm {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    @JoinColumn(name = "consumer_arrowhead_system_id")
    private ArrowheadSystem consumer;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {javax.persistence.CascadeType.MERGE})
    @JoinColumn(name = "provider_arrowhead_system_id")
    private ArrowheadSystem provider;

    @ElementCollection(fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.FALSE)
    @MapKeyColumn(name = "qos_key")
    @Column(name = "qos_value")
    @CollectionTable(name = "monitor_log_qos_parameters", joinColumns = @JoinColumn(name = "id"))
    private Map<String, String> parameters = new HashMap<>();

    public AddLogForm() {
    }

    public AddLogForm(ArrowheadSystem consumer, ArrowheadSystem provider, Map<String, String> parameters) {
        this.consumer = consumer;
        this.provider = provider;
        this.parameters = parameters;
    }

    public ArrowheadSystem getConsumer() {
        return consumer;
    }

    public void setConsumer(ArrowheadSystem consumer) {
        this.consumer = consumer;
    }

    public ArrowheadSystem getProvider() {
        return provider;
    }

    public void setProvider(ArrowheadSystem provider) {
        this.provider = provider;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

}
