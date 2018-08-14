/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.qos.drivers.implementations;

import eu.arrowhead.qos.drivers.IQoSDriver;
import eu.arrowhead.qos.drivers.ReservationInfo;
import eu.arrowhead.qos.drivers.ReservationResponse;
import java.util.HashMap;

/**
 *
 * @author Flavio
 */
public class TESTNETWORKTYPE implements IQoSDriver {

    @Override
    public ReservationResponse reserveQoS(ReservationInfo info) {
        return new ReservationResponse(true, "null", new HashMap<String, String>());
    }

}
