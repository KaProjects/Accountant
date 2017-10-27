package org.kaleta.accountant.frontend;

/**
 * Provides methods for accessing to configuration.
 * Every component which implements this interface is able to retrieve and update app. wide configuration.
 */
public interface Configurable {

    /**
     * Set configuration reference to component. This method is called while initialization of app.
     */
    void setConfiguration(Configuration configuration);

    /**
     * Provides configuration reference to component (and directly to its actions).
     */
    Configuration getConfiguration();
}
