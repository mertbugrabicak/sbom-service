package org.jboss.sbomer.sbom.service.adapter.in.rest.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents a destination where the generated SBOM should be published.
 */
public record PublisherDTO(
        @NotBlank(message = "Publisher name must be provided")
        String name,
        String version,
        Map<String, String> options
) {}
