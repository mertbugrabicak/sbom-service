package org.jboss.sbomer.sbom.service.core.domain.dto;

import java.time.Instant;
import java.util.List;

import org.jboss.sbomer.sbom.service.core.domain.enums.EnhancementStatus;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO representing the state of a single enhancement task.
 * A GenerationRecord can have one or more of these.
 */
@Getter
@Setter
public class EnhancementRecord {

    private String id;
    private String enhancerName;
    private String enhancerVersion;
    /**
     * The 0-based order in which this enhancement step should be executed.
     */
    private int index;
    private Instant created;
    private Instant updated;
    private Instant finished;
    private EnhancementStatus status;
    private Integer result;
    private String reason;
    private String requestId;
    private List<String> enhancedSbomUrls;
    private String generationId;
}
