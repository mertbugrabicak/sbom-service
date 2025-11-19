package org.jboss.sbomer.sbom.service.adapter.out.generation;

import org.apache.avro.io.DatumReader;
import org.apache.avro.specific.SpecificDatumReader;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.sbomer.events.orchestration.GenerationCreated;
import org.jboss.sbomer.events.orchestration.GenerationFinished;
import org.jboss.sbomer.sbom.service.core.domain.dto.GenerationRecord;
import org.jboss.sbomer.sbom.service.core.port.spi.FailureNotifier;
import org.jboss.sbomer.sbom.service.core.port.spi.generation.GenerationFinishedNotifier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

// CURRENTLY NOT NEEDED! MIGHT NOT EVER BE NEEDED
@ApplicationScoped
@Slf4j
public class KafkaGenerationFinishedNotifier implements GenerationFinishedNotifier {

    @Inject @Channel("generation-finished")
    Emitter<GenerationFinished> finishedEmitter;

    @Inject
    FailureNotifier failureNotifier;

    // Use Avro's specific reader for reliable deserialization
    private final DatumReader<GenerationCreated> reader = new SpecificDatumReader<>(GenerationCreated.class);

    @Override
    public void notifyFinished(GenerationRecord record) {
        log.info("Publishing success for Generation ID '{}'", record.getId());
        publishFinishedEvent(record);
    }

    // --- Private Helper Methods ---

    private void publishFinishedEvent(GenerationRecord record) {

    }

}
