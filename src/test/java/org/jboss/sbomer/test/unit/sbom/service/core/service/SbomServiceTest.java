package org.jboss.sbomer.test.unit.sbom.service.core.service;

import org.jboss.sbomer.sbom.service.core.port.spi.generation.GenerationScheduler;
import org.jboss.sbomer.sbom.service.core.service.SbomService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SbomServiceTest {

    @InjectMocks
    private SbomService generationDispatcherService;

    @Mock
    private GenerationScheduler generationScheduler;

}
