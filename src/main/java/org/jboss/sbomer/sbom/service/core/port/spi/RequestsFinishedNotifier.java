package org.jboss.sbomer.sbom.service.core.port.spi;

import org.jboss.sbomer.events.orchestration.RequestsFinished;

public interface RequestsFinishedNotifier {

    void notify(RequestsFinished requestsFinishedEvent);

}
