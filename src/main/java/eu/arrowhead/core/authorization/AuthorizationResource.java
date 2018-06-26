/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.core.authorization;

import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.messages.IntraCloudAuthRequest;
import eu.arrowhead.common.messages.IntraCloudAuthResponse;
import eu.arrowhead.common.messages.TokenGenerationRequest;
import eu.arrowhead.common.messages.TokenGenerationResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * This is the REST resource for the Authorization Core System.
 */
@Path("authorization")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthorizationResource {

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "This is the Authorization Resource.";
  }


  /**
   * Checks whether the consumer System can use a Service from a list of provider Systems.
   *
   * @return IntraCloudAuthResponse
   *
   * @throws DataNotFoundException, BadPayloadException
   */
  @PUT
  @Path("intracloud")
  public Response isSystemAuthorized(IntraCloudAuthRequest request) {
    IntraCloudAuthResponse response = AuthorizationService.isSystemAuthorized(request);
    return Response.status(Status.OK).entity(response).build();
  }

  /**
   * Generates ArrowheadTokens for each consumer/service/provider trio
   *
   * @return TokenGenerationResponse
   */
  @PUT
  @Path("token")
  public Response tokenGeneration(TokenGenerationRequest request) {
    TokenGenerationResponse response = AuthorizationService.tokenGeneration(request);
    return Response.status(Status.OK).entity(response).build();
  }

}
