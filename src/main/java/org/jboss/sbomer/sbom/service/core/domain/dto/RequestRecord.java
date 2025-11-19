package org.jboss.sbomer.sbom.service.core.domain.dto;

import java.util.List;

import org.jboss.sbomer.sbom.service.core.domain.enums.RequestStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestRecord {
    private String id;
    private List<GenerationRecord> generationRecords;
    private List<PublisherRecord> publisherRecords;
    private RequestStatus status;
}
