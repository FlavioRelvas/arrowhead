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
package eu.arrowhead.qos.drivers.implementations;

import eu.arrowhead.qos.drivers.IQoSDriver;
import eu.arrowhead.qos.drivers.ReservationInfo;
import eu.arrowhead.qos.drivers.ReservationResponse;
import java.util.HashMap;

/**
 *
 * @author Flavio
 */
public class WIFI implements IQoSDriver {

    @Override
    public ReservationResponse reserveQoS(ReservationInfo info) {
        return new ReservationResponse(true, "null", new HashMap<String, String>());
    }

}
