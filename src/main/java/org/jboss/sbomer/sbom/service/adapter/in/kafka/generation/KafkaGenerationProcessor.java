package org.jboss.sbomer.sbom.service.adapter.in.kafka.generation;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.sbomer.events.request.RequestsCreated;
import org.jboss.sbomer.sbom.service.core.port.api.generation.GenerationProcessor;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka event listener that processes generation requests
 */
@ApplicationScoped
@Slf4j
public class KafkaGenerationProcessor {

    private GenerationProcessor generationProcessor;

    @Inject
    KafkaGenerationProcessor(GenerationProcessor generationProcessor) {
        this.generationProcessor = generationProcessor;
    }

    @Incoming("requests-created")
    public void processGenerationsFromKafka(RequestsCreated requestsCreated) {
        log.info("Received requests.created event from" + requestsCreated.getContext().getSource() + ". Setting up and dispatching to generators");
        generationProcessor.processGenerations(requestsCreated);
    }
}
