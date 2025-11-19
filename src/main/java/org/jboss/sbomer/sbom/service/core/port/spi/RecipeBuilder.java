package org.jboss.sbomer.sbom.service.core.port.spi;

import org.jboss.sbomer.events.orchestration.Recipe;

/**
 * <p>
 * To create a recipe for an SBOM generation
 * </p>
 */
public interface RecipeBuilder {

    /**
     * Specify an available generator + enhancers for a given type and identifier
     */
    Recipe buildRecipeFor(String type, String identifier);

}
