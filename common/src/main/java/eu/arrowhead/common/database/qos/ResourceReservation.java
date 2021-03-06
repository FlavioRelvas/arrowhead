/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(name = "resource_reservation", uniqueConstraints = {@UniqueConstraint(columnNames = {"consumer_arrowhead_system_id", "provider_arrowhead_system_id"})})
public class ResourceReservation {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "state")
    private String state;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    @JoinColumn(name = "consumer_arrowhead_system_id")
    private ArrowheadSystem consumer;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    @JoinColumn(name = "provider_arrowhead_system_id")
    private ArrowheadSystem provider;

    @ElementCollection(fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.FALSE)
    @MapKeyColumn(name = "qos_key")
    @Column(name = "qos_value")
    @CollectionTable(name = "resource_reservation_qos_parameters", joinColumns = @JoinColumn(name = "id"))
    private Map<String, String> qosParameters = new HashMap<>();

    public ResourceReservation() {
    }

    public ResourceReservation(String state, ArrowheadSystem consumer, ArrowheadSystem provider, Map<String, String> qosParameters) {
        this.state = state;
        this.consumer = consumer;
        this.provider = provider;
        this.qosParameters = qosParameters;
    }

    /**
     * Get ID.
     *
     * @return returns integer with ID
     */
    @XmlTransient
    public int getId() {
        return id;
    }

    /**
     * set ID.
     *
     * @param id integer ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * get state of qos reservation.
     *
     * @return returns state
     */
    public String getState() {
        return state;
    }

    /**
     * set qos reservation state.
     *
     * @param state qos reservation state
     */
    public void setState(String state) {
        this.state = state;
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

    /**
     * get qos parameters.
     *
     * @return returns map with qos parameters
     */
    public Map<String, String> getQosParameters() {
        return qosParameters;
    }

    /**
     * set qos parameters.
     *
     * @param qosParameter map with the qos parameters
     */
    public void setQosParameter(Map<String, String> qosParameter) {
        this.qosParameters = qosParameter;
    }

}
