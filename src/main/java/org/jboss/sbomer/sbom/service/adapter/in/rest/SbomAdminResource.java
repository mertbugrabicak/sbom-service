package org.jboss.sbomer.sbom.service.adapter.in.rest;

import java.util.List;

import org.jboss.sbomer.sbom.service.core.domain.dto.GenerationRecord;
import org.jboss.sbomer.sbom.service.core.domain.dto.RequestRecord;
import org.jboss.sbomer.sbom.service.core.domain.model.Page;
import org.jboss.sbomer.sbom.service.core.port.api.SbomAdministration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

/**
 * REST API Adapter for Administration tasks.
 * Handles reading status and triggering retries.
 */
@Path("/api/v1/admin")
@ApplicationScoped
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SbomAdminResource {

    @Inject
    SbomAdministration sbomAdministration; // The Admin Port

    // --- READ ENDPOINTS ---

    @GET
    @Path("/requests")
    public Response fetchRequests(@QueryParam("page") @DefaultValue("0") int page,
                                  @QueryParam("size") @DefaultValue("20") int size) {
        Page<RequestRecord> result = sbomAdministration.fetchRequests(page, size);
        return Response.ok(result).build();
    }

    @GET
    @Path("/requests/{requestId}/generations")
    public Response fetchGenerations(@PathParam("requestId") String requestId,
                                     @QueryParam("page") @DefaultValue("0") int page,
                                     @QueryParam("size") @DefaultValue("20") int size) {
        Page<GenerationRecord> result = sbomAdministration.fetchGenerations(requestId, page, size);
        return Response.ok(result).build();
    }

    @GET
    @Path("/requests/{requestId}/generations/all")
    public Response getAllGenerationsForRequest(@PathParam("requestId") String requestId) {
        List<GenerationRecord> records = sbomAdministration.getGenerationsForRequest(requestId);

        if (records == null || records.isEmpty()) {
            // Maybe return 404 if the request ID doesn't exist,
            // just empty list 200 is okay for now
            // TODO throw an error
        }

        return Response.ok(records).build();
    }

    @GET
    @Path("/generations/{id}")
    public Response getGeneration(@PathParam("id") String generationId) {
        GenerationRecord record = sbomAdministration.getGeneration(generationId);
        if (record == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(record).build();
    }

    // --- ACTION ENDPOINTS ---

    @POST
    @Path("/generations/{id}/retry")
    public Response retryGeneration(@PathParam("id") String generationId) {
        try {
            sbomAdministration.retryGeneration(generationId);
            return Response.accepted().entity("Retry scheduled").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        } catch (Exception e) {
            log.error("Failed to retry generation {}", generationId, e);
            return Response.serverError().entity("Internal error").build();
        }
    }

    @POST
    @Path("/enhancements/{id}/retry")
    public Response retryEnhancement(@PathParam("id") String enhancementId) {
        try {
            sbomAdministration.retryEnhancement(enhancementId);
            return Response.accepted().entity("Retry scheduled").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        } catch (Exception e) {
            log.error("Failed to retry enhancement {}", enhancementId, e);
            return Response.serverError().entity("Internal error").build();
        }
    }
}
