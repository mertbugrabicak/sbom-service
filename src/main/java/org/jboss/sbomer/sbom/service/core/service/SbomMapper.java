package org.jboss.sbomer.sbom.service.core.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jboss.sbomer.events.common.*;
import org.jboss.sbomer.events.orchestration.*;
import org.jboss.sbomer.events.request.RequestsCreated;
import org.jboss.sbomer.sbom.service.core.ApplicationConstants;
import org.jboss.sbomer.sbom.service.core.domain.dto.EnhancementRecord;
import org.jboss.sbomer.sbom.service.core.domain.dto.GenerationRecord;
import org.jboss.sbomer.sbom.service.core.domain.dto.PublisherRecord;
import org.jboss.sbomer.sbom.service.core.domain.dto.RequestRecord;
import org.jboss.sbomer.sbom.service.core.domain.enums.EnhancementStatus;
import org.jboss.sbomer.sbom.service.core.domain.enums.GenerationStatus;
import org.jboss.sbomer.sbom.service.core.domain.enums.RequestStatus;
import org.jboss.sbomer.sbom.service.core.port.spi.RecipeBuilder;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SbomMapper {

    RecipeBuilder recipeBuilder;

    public SbomMapper(RecipeBuilder recipeBuilder) {
        this.recipeBuilder = recipeBuilder;
    }

    /**
     * Create the Database Record from the incoming Request Spec.
     * This includes running the RecipeBuilder to determine what generators/enhancers are needed.
     */
    public RequestRecord toNewRequestRecord(RequestsCreated requestsCreated) {
        List<PublisherRecord> publisherRecords = requestsCreated.getData().getPublishers().stream()
                .map(this::toPublisherRecord)
                .toList();
        RequestRecord requestRecord = new RequestRecord();
        requestRecord.setId(requestsCreated.getData().getRequestId());
        requestRecord.setPublisherRecords(publisherRecords);
        requestRecord.setStatus(RequestStatus.NEW);
        return requestRecord;
    }

    public PublisherRecord toPublisherRecord(PublisherSpec spec) {
        PublisherRecord record = new PublisherRecord();
        record.setName(spec.getName());
        record.setVersion(spec.getVersion());
        return record;
    }

    public PublisherSpec toPublisherSpec(PublisherRecord publisherRecord) {
        PublisherSpec spec = PublisherSpec.newBuilder()
                .setName(publisherRecord.getName())
                .setVersion(publisherRecord.getVersion())
                .build();
        return spec;
    }

    /**
     * Reconstructs a Request Spec from the stored Record.
     * Used when we need to retry a generation.
     */
    public GenerationRequestSpec toGenerationRequestSpec(GenerationRecord record) {
        Target target = Target.newBuilder()
                .setType(record.getTargetType())
                .setIdentifier(record.getTargetIdentifier())
                .build();

        return GenerationRequestSpec.newBuilder()
                .setGenerationId(record.getId())
                .setTarget(target)
                .build();
    }


    public GenerationRecord toNewGenerationRecord(GenerationRequestSpec requestSpec, String requestId) {
        // Build the Recipe (Determine generators and enhancers needed for type and identifier)
        Recipe recipe = recipeBuilder.buildRecipeFor(requestSpec.getTarget().getType(), requestSpec.getTarget().getIdentifier());

        // Create a parent Generation Record
        GenerationRecord generationRecord = new GenerationRecord();
        generationRecord.setId(requestSpec.getGenerationId());
        generationRecord.setGeneratorName(recipe.getGenerator().getName());
        generationRecord.setGeneratorVersion(recipe.getGenerator().getVersion());
        generationRecord.setCreated(Instant.now()); // Timestamp created here
        generationRecord.setUpdated(Instant.now());
        generationRecord.setStatus(GenerationStatus.NEW);
        generationRecord.setRequestId(requestId);
        generationRecord.setTargetType(requestSpec.getTarget().getType());
        generationRecord.setTargetIdentifier(requestSpec.getTarget().getIdentifier());

        // Create child Enhancement Records based on the Recipe
        List<EnhancementRecord> enhancementRecords = new ArrayList<>();
        List<EnhancerSpec> enhancerSpecs = recipe.getEnhancers();

        if (enhancerSpecs != null) {
            for (int i = 0; i < enhancerSpecs.size(); i++) {
                EnhancementRecord enhancementRecord = new EnhancementRecord();
                enhancementRecord.setId(UUID.randomUUID().toString());
                enhancementRecord.setEnhancerName(enhancerSpecs.get(i).getName());
                enhancementRecord.setEnhancerVersion(enhancerSpecs.get(i).getVersion());
                enhancementRecord.setIndex(i); // Preserve order
                enhancementRecord.setCreated(Instant.now());
                enhancementRecord.setUpdated(Instant.now());
                enhancementRecord.setStatus(EnhancementStatus.NEW);
                enhancementRecord.setRequestId(requestId);
                enhancementRecord.setGenerationId(generationRecord.getId());
                enhancementRecords.add(enhancementRecord);
            }
        }

        generationRecord.setEnhancements(enhancementRecords);
        return generationRecord;
    }

    public GenerationCreated toGenerationCreatedEvent(GenerationRecord generationRecord, GenerationRequestSpec originalSpec, String correlationId) {

        // Reconstruct the Recipe object from the saved Record
        // This guarantees the event payload matches the DB state
        GeneratorSpec generatorSpec = GeneratorSpec.newBuilder()
                .setName(generationRecord.getGeneratorName())
                .setVersion(generationRecord.getGeneratorVersion())
                .build();

        List<EnhancerSpec> enhancerSpecs = generationRecord.getEnhancements().stream()
                .sorted(Comparator.comparingInt(EnhancementRecord::getIndex))
                .map(r -> EnhancerSpec.newBuilder().setName(r.getEnhancerName()).setVersion(r.getEnhancerVersion()).build())
                .collect(Collectors.toList());

        Recipe recipe = Recipe.newBuilder()
                .setGenerator(generatorSpec)
                .setEnhancers(enhancerSpecs)
                .build();

        // Build the Context
        ContextSpec context = ContextSpec.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setType("GenerationCreated")
                .setSource(ApplicationConstants.COMPONENT_NAME)
                .setCorrelationId(correlationId)
                .setEventId("1.0")
                .setTimestamp(generationRecord.getCreated()) // Use the Record's timestamp for consistency
                .build();

        // Build the Data
        GenerationData generationData = GenerationData.newBuilder()
                .setRequestId(generationRecord.getRequestId())
                .setGenerationRequest(originalSpec)
                .setRecipe(recipe)
                .build();

        return GenerationCreated.newBuilder()
                .setContext(context)
                .setData(generationData)
                .build();
    }

    public EnhancementCreated toEnhancementCreatedEvent(EnhancementRecord current, EnhancementRecord lastFinished, GenerationRecord parentGeneration) {
        // First build the context
        ContextSpec context = ContextSpec.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setType("EnhancementCreated")
                .setSource(ApplicationConstants.COMPONENT_NAME)
                .setCorrelationId(current.getRequestId())
                .setEventId("1.0")
                .setTimestamp(Instant.now())
                .build();

        // Define the enhancer name and version
        EnhancerSpec enhancerSpec = EnhancerSpec.newBuilder()
                .setName(current.getEnhancerName())
                .setVersion(current.getEnhancerVersion())
                .build();

        List<String> inputSbomUrls;
        if (lastFinished != null) {
            // if a previous enhancement has occured, use those enhanced SBOMs as input
            inputSbomUrls = lastFinished.getEnhancedSbomUrls();
        } else {
            // if no previous enhancement has occured, use the base SBOMs from the generation
            inputSbomUrls = parentGeneration.getGenerationSbomUrls();
        }

        EnhancementData enhancementData = EnhancementData.newBuilder()
                .setEnhancementId(current.getId())
                .setGenerationId(current.getGenerationId())
                .setRequestId(current.getRequestId())
                .setEnhancer(enhancerSpec)
                .setInputSbomUrls(inputSbomUrls)
                .build();

        // EnhancementCreated has been constructed
        return EnhancementCreated.newBuilder()
                .setContext(context)
                .setData(enhancementData)
                .build();
    }

    public RequestsFinished toRequestsFinishedEvent(RequestRecord requestRecord) {

        // First build the context
        ContextSpec context = ContextSpec.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setType("RequestsFinished")
                .setSource(ApplicationConstants.COMPONENT_NAME)
                .setCorrelationId(requestRecord.getId())
                .setEventId("1.0")
                .setTimestamp(Instant.now())
                .build();

        List<CompletedGeneration>  completedGenerations = new ArrayList<>();
        for (GenerationRecord generationRecord : requestRecord.getGenerationRecords()) {
            Target target = Target.newBuilder()
                    .setType(generationRecord.getTargetType())
                    .setIdentifier(generationRecord.getTargetIdentifier())
                    .build();
            GenerationRequestSpec generationRequestSpec = GenerationRequestSpec.newBuilder()
                    .setGenerationId(generationRecord.getId())
                    .setTarget(target)
                    .build();
            CompletedGeneration completedGeneration = CompletedGeneration.newBuilder()
                    .setGenerationRequest(generationRequestSpec)
                    .setFinalSbomUrls(determineFinalUrls(generationRecord))
                    .build();

            completedGenerations.add(completedGeneration);
        }

        List<PublisherSpec> publisherSpecs = new ArrayList<>();
        if (requestRecord.getPublisherRecords() != null) {
            publisherSpecs = requestRecord.getPublisherRecords().stream()
                    .map(this::toPublisherSpec)
                    .toList();
        }

        RequestsFinishedData requestsFinishedData = RequestsFinishedData.newBuilder()
                .setRequestId(requestRecord.getId())
                .setCompletedGenerations(completedGenerations)
                .setPublishers(publisherSpecs) // TODO
                .build();

        RequestsFinished requestsFinished = RequestsFinished.newBuilder()
                .setContext(context)
                .setData(requestsFinishedData)
                .build();


        return requestsFinished;
    }

    private List<String> determineFinalUrls(GenerationRecord record) {
        // If no enhancements, or list is null, return base generation URLs
        if (record.getEnhancements() == null || record.getEnhancements().isEmpty()) {
            return record.getGenerationSbomUrls();
        }

        // Find the enhancement with the highest index that has URLs
        // Since this is only called when requests are finished, we assume the chain completed successfully
        return record.getEnhancements().stream()
                .max(Comparator.comparingInt(EnhancementRecord::getIndex))
                .map(EnhancementRecord::getEnhancedSbomUrls)
                .orElse(record.getGenerationSbomUrls());
    }

}
