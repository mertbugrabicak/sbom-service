package org.jboss.sbomer.sbom.service.core.port.spi.enhancement;

import org.jboss.sbomer.events.orchestration.EnhancementCreated;

/**
 * <p>
 * Primary interface for scheduling individual SBOM enhancements
 * </p>
 */
public interface EnhancementScheduler {
    void schedule(EnhancementCreated enhancementCreated);
}
