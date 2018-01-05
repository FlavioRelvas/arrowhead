package eu.arrowhead.core.orchestrator.api;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.OrchestrationStore;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.messages.OrchestrationStoreQuery;
import eu.arrowhead.core.orchestrator.StoreService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Logger;

@Path("mgmt/store")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StoreApi {

  private static final Logger log = Logger.getLogger(StoreApi.class.getName());

  private final DatabaseManager dm = DatabaseManager.getInstance();
  private final HashMap<String, Object> restrictionMap = new HashMap<>();


  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "orchestrator/mgmt/store got it";
  }

  /**
   * Returns an Orchestration Store entry from the database specified by the database generated id.
   *
   * @return OrchestrationStore
   */
  @GET
  @Path("{id}")
  public Response getStoreEntry(@PathParam("id") int id) {

    restrictionMap.put("id", id);
    OrchestrationStore entry = dm.get(OrchestrationStore.class, restrictionMap);
    if (entry == null) {
      log.info("StoreApi:getStoreEntry throws DataNotFoundException");
      throw new DataNotFoundException("Requested store entry was not found in the database.");
    } else {
      log.info("getStoreEntry returns a store entry.");
      return Response.ok(entry).build();
    }
  }

  /**
   * Returns all the entries of the Orchestration Store.
   *
   * @return List<OrchestrationStore>
   */
  @GET
  @Path("all")
  public List<OrchestrationStore> getAllStoreEntries() {

    List<OrchestrationStore> store = dm.getAll(OrchestrationStore.class, restrictionMap);
    if (store.isEmpty()) {
      log.info("StoreApi:getAllStoreEntries throws DataNotFoundException.");
      throw new DataNotFoundException("The Orchestration Store is empty.");
    }

    Collections.sort(store);
    log.info("getAllStoreEntries successfully returns.");
    return store;
  }

  /**
   * Returns all the default entries of the Orchestration Store.
   *
   * @return List<OrchestrationStore>
   */
  @GET
  @Path("all_default")
  public List<OrchestrationStore> getDefaultStoreEntries() {

    restrictionMap.put("defaultEntry", true);
    List<OrchestrationStore> store = dm.getAll(OrchestrationStore.class, restrictionMap);
    if (store.isEmpty()) {
      log.info("StoreApi:getDefaultStoreEntries throws DataNotFoundException.");
      throw new DataNotFoundException("Default Orchestration Store entries were not found.");
    }

    Collections.sort(store);
    log.info("getDefaultStoreEntries successfully returns.");
    return store;
  }

  /**
   * Returns the Orchestration Store entries from the database specified by the consumer (and the service).
   *
   * @return List<OrchestrationStore>
   *
   * @throws BadPayloadException, DataNotFoundException
   */
  @PUT
  public Response getStoreEntries(OrchestrationStoreQuery query) {

    if (!query.isValid()) {
      log.info("StoreApi:getStoreEntries throws BadPayloadException.");
      throw new BadPayloadException("Bad payload: mandatory field(s) of requesterSystem or requestedService is/are missing.");
    }

    List<OrchestrationStore> store = new ArrayList<>();
    if (query.getRequestedService() == null) {
      StoreService.getDefaultStoreEntries(query.getRequesterSystem());
    } else if (query.getRequesterSystem() == null) {
      StoreService.getStoreEntries(query.getRequestedService());
    } else {
      StoreService.getStoreEntries(query.getRequesterSystem(), query.getRequestedService());
    }

    Collections.sort(store);
    GenericEntity<List<OrchestrationStore>> entity = new GenericEntity<List<OrchestrationStore>>(store) {
    };
    log.info("getStoreEntries successfully returns.");
    return Response.ok(entity).build();
  }

  /**
   * Adds a list of Orchestration Store entries to the database. Elements which would throw BadPayloadException are being skipped. The returned list
   * only contains the elements which were saved in the process.
   *
   * @return List<OrchestrationStore>
   */

  @POST
  public List<OrchestrationStore> addStoreEntries(List<OrchestrationStore> storeEntries) {

    List<OrchestrationStore> store = new ArrayList<>();
    for (OrchestrationStore entry : storeEntries) {
      if (entry.isValid()) {
        restrictionMap.clear();
        restrictionMap.put("systemGroup", entry.getConsumer().getSystemGroup());
        restrictionMap.put("systemName", entry.getConsumer().getSystemName());
        ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);
        if (consumer == null) {
          consumer = dm.save(entry.getConsumer());
        }

        restrictionMap.clear();
        restrictionMap.put("serviceGroup", entry.getService().getServiceGroup());
        restrictionMap.put("serviceDefinition", entry.getService().getServiceDefinition());
        ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
        if (service == null) {
          service = dm.save(entry.getService());
        }

        ArrowheadCloud providerCloud = null;
        if (entry.getProviderCloud() != null && entry.getProviderCloud().isValid()) {
          restrictionMap.clear();
          restrictionMap.put("operator", entry.getProviderCloud().getOperator());
          restrictionMap.put("cloudName", entry.getProviderCloud().getCloudName());
          providerCloud = dm.get(ArrowheadCloud.class, restrictionMap);
          if (providerCloud == null) {
            providerCloud = dm.save(entry.getProviderCloud());
          }
        }

        ArrowheadSystem providerSystem = null;
        if (entry.getProviderSystem() != null && entry.getProviderSystem().isValid()) {
          restrictionMap.clear();
          restrictionMap.put("systemGroup", entry.getProviderSystem().getSystemGroup());
          restrictionMap.put("systemName", entry.getProviderSystem().getSystemName());
          providerSystem = dm.get(ArrowheadSystem.class, restrictionMap);
          if (providerSystem == null) {
            providerSystem = dm.save(entry.getProviderSystem());
          }
        }

        entry.setConsumer(consumer);
        entry.setService(service);
        entry.setProviderSystem(providerSystem);
        entry.setProviderCloud(providerCloud);
        entry.setLastUpdated(new Date());
        dm.merge(entry);
        store.add(entry);
      }
    }

    log.info("addStoreEntries successfully returns. Arraylist size: " + store.size());
    return store;
  }

  /**
   * Toggles the <tt>defaultEntry</tt> boolean for the Orchestration Store entry specified by the id field.
   *
   * @return OrchestrationStore
   *
   * @throws DataNotFoundException, BadPayloadException
   */
  @GET
  @Path("default/{id}")
  public Response toggleIsDefault(@PathParam("id") int id) {

    restrictionMap.put("id", id);
    OrchestrationStore entry = dm.get(OrchestrationStore.class, restrictionMap);
    if (entry == null) {
      log.info("StoreApi:toggleIsDefault throws DataNotFoundException.");
      throw new DataNotFoundException("Orchestration Store entry with this id was not found in the database.");
    } else if (entry.getProviderCloud() != null && !entry.isDefaultEntry()) {
      log.info("StoreApi:toggleIsDefault throws BadPayloadException.");
      throw new BadPayloadException("Only intra-cloud store entries can be set as default entries.");
    } else {
      entry.setDefaultEntry(!entry.isDefaultEntry());
      dm.merge(entry);
      log.info("toggleIsDefault succesfully returns.");
      return Response.ok(entry).build();
    }
  }

  /**
   * Updates the non-entity fields of an Orchestration Store entry specified by the id field of the payload. Entity fields have their own update
   * method in CommonApi.class. (Or delete and then post the modified entry again.)
   *
   * @return OrchestrationStore
   *
   * @throws BadPayloadException, DataNotFoundException
   */
  @PUT
  @Path("update")
  public Response updateEntry(OrchestrationStore payload) {

    if (payload.getId() == 0) {
      log.info("StoreApi:updateEntry throws BadPayloadException.");
      throw new BadPayloadException("Bad payload: id field is missing from the payload.");
    }

    restrictionMap.put("id", payload.getId());
    OrchestrationStore storeEntry = dm.get(OrchestrationStore.class, restrictionMap);
    if (storeEntry == null) {
      log.info("StoreApi:updateEntry throws DataNotFoundException.");
      throw new DataNotFoundException("Store entry specified by the id(" + payload.getId() + ") was not found in the database.");
    } else if (storeEntry.getProviderCloud() != null && payload.isDefaultEntry()) {
      log.info("StoreApi:toggleIsDefault throws BadPayloadException.");
      throw new BadPayloadException("Only intra-cloud store entries can be set as default entries.");
    } else {
      storeEntry.setPriority(payload.getPriority());
      storeEntry.setDefaultEntry(payload.isDefaultEntry());
      storeEntry.setName(payload.getName());
      storeEntry.setLastUpdated(new Date());
      storeEntry.setInstruction(payload.getInstruction());
      storeEntry.setAttributes(payload.getAttributes());
      storeEntry.setServiceURI(payload.getServiceURI());
      storeEntry = dm.merge(storeEntry);

      log.info("updateEntry successfully returns.");
      return Response.status(Status.ACCEPTED).entity(storeEntry).build();
    }
  }

  /**
   * Deletes the Orchestration Store entry with the id specified by the path parameter. Returns 200 if the delete is successful, 204 (no content) if
   * the entry was not in the database to begin with.
   */
  @DELETE
  @Path("{id}")
  public Response deleteEntry(@PathParam("id") Integer id) {

    restrictionMap.put("id", id);
    OrchestrationStore entry = dm.get(OrchestrationStore.class, restrictionMap);
    if (entry == null) {
      log.info("deleteEntry had no effect.");
      return Response.noContent().build();
    } else {
      dm.delete(entry);
      log.info("deleteEntry successfully returns.");
      return Response.ok().build();
    }
  }

  /**
   * Deletes the Orchestration Store entries from the database specified by the consumer. Returns 200 if the delete is successful, 204 (no content) if
   * no matching entries were in the database to begin with.
   */
  @DELETE
  @Path("systemgroup/{systemGroup}/systemname/{systemName}")
  public Response deleteEntries(@PathParam("systemGroup") String systemGroup, @PathParam("systemName") String systemName) {

    restrictionMap.put("systemGroup", systemGroup);
    restrictionMap.put("systemName", systemName);
    ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);

    restrictionMap.clear();
    restrictionMap.put("consumer", consumer);
    List<OrchestrationStore> store = dm.getAll(OrchestrationStore.class, restrictionMap);
    if (store.isEmpty()) {
      log.info("deleteEntries had no effect.");
      return Response.noContent().build();
    } else {
      for (OrchestrationStore entry : store) {
        dm.delete(entry);
      }

      log.info("deleteEntries successfully returns.");
      return Response.ok().build();
    }
  }

}