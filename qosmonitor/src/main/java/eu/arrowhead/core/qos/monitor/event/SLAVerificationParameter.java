/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.core.qos.monitor.event;

import java.util.Objects;

/**
 *
 * @author Renato Ayres
 */
public class SLAVerificationParameter {

    private String name;
    private Double requestedValue;
    private Double loggedValue;

    public SLAVerificationParameter(String name, Double requestedValue, Double loggedValue) {
        this.name = name;
        this.requestedValue = requestedValue;
        this.loggedValue = loggedValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getRequestedValue() {
        return requestedValue;
    }

    public void setRequestedValue(Double requestedValue) {
        this.requestedValue = requestedValue;
    }

    public Double getLoggedValue() {
        return loggedValue;
    }

    public void setLoggedValue(Double loggedValue) {
        this.loggedValue = loggedValue;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        SLAVerificationParameter parameter = (SLAVerificationParameter) obj;

        if (!this.name.equalsIgnoreCase(parameter.name)) {
            return false;
        }

        if (!Objects.equals(this.requestedValue, parameter.requestedValue)) {
            return false;
        }

        return Objects.equals(this.loggedValue, parameter.loggedValue);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.name);
        hash = 97 * hash + Objects.hashCode(this.requestedValue);
        hash = 97 * hash + Objects.hashCode(this.loggedValue);
        return hash;
    }
}
