package org.jboss.sbomer.sbom.service.adapter.in.rest.model;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Paginated response wrapper")
public class Page<T> {
    @Schema(description = "The list of results")
    private List<T> content;
    @Schema(description = "Total number of items across all pages")
    private long totalHits;
    @Schema(description = "Total pages")
    private int totalPages;
    @Schema(description = "Page index")
    private int pageIndex;
    @Schema(description = "Page size")
    private int pageSize;
}
