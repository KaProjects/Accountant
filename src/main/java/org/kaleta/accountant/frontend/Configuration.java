package org.kaleta.accountant.frontend;

/**
 * Created by Stanislav Kaleta on 19.04.2016.
 *
 *  Provides major app. wide configuration operations.
 */
public interface Configuration {
    /**
     * list of commands
     */
    int INIT_CONFIG = 0;
    int TRANSACTION_ACTION = 1;

    /**
     * Recursively updates component's tree of app.
     * Every component which contains action registered with key equals to command,
     * will be update according to that action.
     * @param command represents type of config. update
     */
    void update(int command);

    /**
     * Returns actual year according configuration.
     */
    int getActiveYear();
}
