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

import com.google.gson.Gson;
import eu.arrowhead.common.database.qos.AddLogForm;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;

/**
 * Root resource (exposed at "monitor" path).
 *
 * @author Renato Ayres & Flávio
 */
@Path("monitor")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class QoSMonitorResource {

    private final QoSMonitorService monitor = new QoSMonitorService();
    private static final Logger LOG = Logger.getLogger(QoSMonitorResource.class.getName());

    /**
     * Method handling HTTP GET requests in /online path. The returned object
     * will be sent to the client as "text/plain" media type.
     *
     * Used for testing purposes only.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Online.";
    }

    /**
     *
     * @param message
     * @return
     */
    @PUT
    @Path("newlog")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response newLog(String message) {
        try {
            Gson gson = new Gson();
            AddLogForm m = gson.fromJson(message, AddLogForm.class);
            monitor.newLog(m);
        } catch (IllegalAccessError ex) {
            LOG.error(ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ex.getMessage())
                    .build();
        }
        return Response.ok("OK").build();
    }

    @POST
    @Path("feedback")
    public Response receiveEvent(String message) {
        try {
            Gson gson = new Gson();
            Map<String, Boolean> results = gson.fromJson(message, HashMap.class);
            System.out.println("Event publishing results:");
            System.out.println(results.toString());
            return Response.ok().build();
        } catch (IllegalAccessError ex) {
            LOG.error(ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ex.getMessage())
                    .build();
        }
    }

}
