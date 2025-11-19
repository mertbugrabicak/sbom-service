package org.jboss.sbomer.sbom.service.core.port.api.enhancement;

import org.jboss.sbomer.events.enhancer.EnhancementUpdate;

/**
 * Endpoint to track and update the status of an ongoing enhancement
 */
public interface EnhancementStatusProcessor {
    void processEnhancementStatusUpdate(EnhancementUpdate enhancementUpdate);
}
