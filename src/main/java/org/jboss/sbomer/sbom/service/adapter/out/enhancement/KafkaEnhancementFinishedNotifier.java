package org.jboss.sbomer.sbom.service.adapter.out.enhancement;

import java.time.Instant;
import java.util.UUID;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.sbomer.events.common.ContextSpec;
import org.jboss.sbomer.events.orchestration.EnhancementFinished;
import org.jboss.sbomer.sbom.service.core.domain.dto.EnhancementRecord;
import org.jboss.sbomer.sbom.service.core.port.spi.FailureNotifier;
import org.jboss.sbomer.sbom.service.core.port.spi.enhancement.EnhancementFinishedNotifier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

// CURRENTLY NOT NEEDED! MIGHT NOT EVER BE NEEDED
@ApplicationScoped
@Slf4j
public class KafkaEnhancementFinishedNotifier implements EnhancementFinishedNotifier {

    @Inject @Channel("enhancement-finished")
    Emitter<EnhancementFinished> finishedEmitter;

    @Inject
    FailureNotifier failureNotifier;

    @Override
    public void notifyFinished(EnhancementRecord record) {
        log.info("Publishing success for Enhancement ID '{}'", record.getId());
    }

    private ContextSpec createNewContext() {
        ContextSpec context = new ContextSpec();
        context.setEventId(UUID.randomUUID().toString());
        context.setSource("sbomer-generator");
        context.setTimestamp(Instant.now());
        context.setEventVersion("1.0");
        return context;
    }
}
