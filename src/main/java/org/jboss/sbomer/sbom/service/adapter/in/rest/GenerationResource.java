package org.jboss.sbomer.sbom.service.adapter.in.rest;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jboss.sbomer.events.common.ContextSpec;
import org.jboss.sbomer.events.common.GenerationRequestSpec;
import org.jboss.sbomer.events.common.PublisherSpec;
import org.jboss.sbomer.events.common.Target;
import org.jboss.sbomer.events.request.RequestData;
import org.jboss.sbomer.events.request.RequestsCreated;
import org.jboss.sbomer.sbom.service.adapter.in.rest.dto.GenerationRequestDTO;
import org.jboss.sbomer.sbom.service.adapter.in.rest.dto.GenerationRequestsDTO;
import org.jboss.sbomer.sbom.service.adapter.in.rest.dto.PublisherDTO;
import org.jboss.sbomer.sbom.service.core.port.api.generation.GenerationProcessor;
import org.jboss.sbomer.sbom.service.core.utility.TsidUtility;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

/**
 * JAX-RS Resource (REST API Adapter) for triggering SBOM generation.
 */
@Path("/api/v1/generations")
@ApplicationScoped
@Slf4j
public class GenerationResource {

    @Inject
    GenerationProcessor generationProcessor; // The "Port"

    /**
     * Triggers a new SBOM generation from a REST request.
     * @param request The DTO containing generation requests and publishers.
     * @return A 202 Accepted response with the ID of the created request batch.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response triggerGeneration(@Valid GenerationRequestsDTO request) {

        log.info("Received REST request to trigger {} generation requests", request.generationRequests().size());

        // 1. Translate the REST DTO into the internal Avro event object
        RequestsCreated requestsCreatedEvent = toRequestsCreatedEvent(request);

        // 2. Pass the event to the core business logic (the "Port")
        generationProcessor.processGenerations(requestsCreatedEvent);

        // 3. Return a 202 Accepted response, as this is an async process.
        //    We return the batch RequestId so the user can track it.
        String requestId = requestsCreatedEvent.getData().getRequestId();
        return Response.accepted(Collections.singletonMap("id", requestId)).build();
    }

    /**
     * Helper method to map our public DTOs to the internal Avro-generated event object.
     */
    private RequestsCreated toRequestsCreatedEvent(GenerationRequestsDTO request) {
        String newRequestId = TsidUtility.createUniqueGenerationRequestId();
        // Create a new Context based on the correct Avro schema.
        ContextSpec context = ContextSpec.newBuilder()
                .setCorrelationId(newRequestId)
                .setEventId(UUID.randomUUID().toString())
                .setSource("sbomer-rest-api") // Identifies this adapter as the source
                .setEventVersion("1.0") // As per the schema default
                .setTimestamp(Instant.now()) // Current time in UTC millis
                .build();

        // Map Publisher DTOs to Avro PublisherSpecs
        List<PublisherSpec> publishers = Optional.ofNullable(request.publishers())
                .orElse(Collections.emptyList())
                .stream()
                .map(this::toPublisherSpec)
                .collect(Collectors.toList());

        // Map GenerationRequest DTOs to Avro GenerationRequestSpecs
        List<GenerationRequestSpec> generationRequests = request.generationRequests().stream()
                .map(this::toGenerationRequestSpec)
                .collect(Collectors.toList());

        // Create the main data spec, generating a new batch RequestId
        RequestData requestData = RequestData.newBuilder()
                .setRequestId(newRequestId)
                .setGenerationRequests(generationRequests)
                .setPublishers(publishers)
                .build();

        // Build the final event object
        return RequestsCreated.newBuilder()
                .setContext(context)
                .setData(requestData)
                .build();
    }

    private PublisherSpec toPublisherSpec(PublisherDTO dto) {
        return PublisherSpec.newBuilder()
                .setName(dto.name())
                .setVersion(dto.version())
                .setOptions(Optional.ofNullable(dto.options()).orElse(Map.of()))
                .build();
    }

    private GenerationRequestSpec toGenerationRequestSpec(GenerationRequestDTO dto) {
        Target target = Target.newBuilder()
                .setType(dto.target().type())
                .setIdentifier(dto.target().identifier())
                .build();

        return GenerationRequestSpec.newBuilder()
                // A new, unique ID for this specific generation task
                .setGenerationId(TsidUtility.createUniqueGenerationId())
                .setTarget(target)
                .build();
    }
}
