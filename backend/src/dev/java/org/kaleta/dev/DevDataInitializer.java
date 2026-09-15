package org.kaleta.dev;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.kaleta.service.SyncService;

/**
 * Syncs the sample export in src/dev/resources/data into the in-memory database
 * on every start.
 * <p>
 * The dev database is H2 in-memory, so it is empty again after every restart and
 * every live-reload. Without this the schema, account and transaction tables stay
 * empty until /sync/all is called by hand. This class lives in src/dev/java, which
 * only the Maven "dev" profile adds to the build, so it is never part of a
 * production artifact.
 */
@ApplicationScoped
public class DevDataInitializer
{
    private static final Logger LOG = Logger.getLogger(DevDataInitializer.class);

    @ConfigProperty(name = "data.location")
    String dataLocation;

    @Inject
    SyncService syncService;

    void onStart(@Observes StartupEvent event)
    {
        try {
            int years = 0;
            for (String year : syncService.getYears(dataLocation)) {
                syncService.sync(dataLocation + year);
                years++;
            }
            LOG.infof("Dev sample data synced from %s (%d years).", dataLocation, years);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to sync dev sample data from %s.", dataLocation);
        }
    }
}
