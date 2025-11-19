package org.jboss.sbomer.sbom.service.adapter.out.enhancement;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.sbomer.events.orchestration.EnhancementCreated;
import org.jboss.sbomer.sbom.service.core.port.spi.enhancement.EnhancementScheduler;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class KafkaEnhancementScheduler implements EnhancementScheduler {

    @Channel("enhancement-created")
    Emitter<EnhancementCreated> emitter;

    @Override
    public void schedule(EnhancementCreated enhancementCreated) {
        emitter.send(enhancementCreated);
        log.debug("Sent enhancement event {}", enhancementCreated.toString());
    }
}
