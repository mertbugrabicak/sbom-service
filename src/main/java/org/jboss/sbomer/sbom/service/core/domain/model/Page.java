package org.jboss.sbomer.sbom.service.core.domain.model;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Page<T> {
    private List<T> content;
    private long totalHits; // Total number of items across all pages
    private int totalPages;
    private int pageIndex;
    private int pageSize;
}
