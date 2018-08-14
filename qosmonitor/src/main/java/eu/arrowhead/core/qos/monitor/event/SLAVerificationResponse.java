/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.core.qos.monitor.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Renato Ayres
 */
public class SLAVerificationResponse {

    private boolean SLABroken;
    private List<SLAVerificationParameter> parameters;

    public SLAVerificationResponse() {
        this.SLABroken = false;
        this.parameters = new ArrayList();
    }

    public SLAVerificationResponse(List<SLAVerificationParameter> parameters) {
        this.SLABroken = !parameters.isEmpty();
        this.parameters = parameters;
    }

    public boolean isSLABroken() {
        return SLABroken;
    }

    public List<SLAVerificationParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<SLAVerificationParameter> parameters) {
        this.SLABroken = true;
        this.parameters = parameters;
    }

    public boolean addParameter(SLAVerificationParameter parameter) {
        this.SLABroken = true;
        return this.parameters.add(parameter);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        SLAVerificationResponse response = (SLAVerificationResponse) obj;

        if (this.SLABroken != response.SLABroken) {
            return false;
        }

        List<SLAVerificationParameter> responseParameters = response.getParameters();

        return parameters.stream().noneMatch((parameter) -> (!responseParameters.contains(parameter)));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.SLABroken ? 1 : 0);
        hash = 67 * hash + Objects.hashCode(this.parameters);
        return hash;
    }
}
