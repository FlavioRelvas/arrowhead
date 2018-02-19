package eu.arrowhead.qos.filter;

import eu.arrowhead.common.Utility;
import eu.arrowhead.qos.QoSMain;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.USER)
public class InboundDebugFilter implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    if (QoSMain.DEBUG_MODE) {
      System.out.println("New " + requestContext.getMethod() + " request at: " + requestContext.getUriInfo().getRequestUri().toString());
      String prettyJson = Utility.getRequestPayload(requestContext.getEntityStream());
      System.out.println(prettyJson);

      InputStream in = new ByteArrayInputStream(prettyJson.getBytes("UTF-8"));
      requestContext.setEntityStream(in);
    }
  }
}
