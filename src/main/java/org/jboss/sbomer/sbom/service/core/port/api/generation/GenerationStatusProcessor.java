package org.jboss.sbomer.sbom.service.core.port.api.generation;

import org.jboss.sbomer.events.generator.GenerationUpdate;

/**
 * Endpoint to track and update the status of an ongoing generation
 */
public interface GenerationStatusProcessor {
    void processGenerationStatusUpdate(GenerationUpdate generationUpdate);
}
