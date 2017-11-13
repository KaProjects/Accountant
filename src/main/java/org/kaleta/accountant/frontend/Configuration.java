package org.kaleta.accountant.frontend;


/**
 *  Provides major app. wide configuration operations.
 */
public interface Configuration {
    /**
     * list of commands
     */
    int INIT_CONFIG = 0;
    int YEAR_ADDED = 1;
    int YEAR_SELECTED = 2;
    int SCHEMA_UPDATED = 3;
    int ACCOUNT_UPDATED = 4;
    int TRANSACTION_UPDATED = 5;
    int PROCEDURE_UPDATED = 6;

    /**
     * Recursively updates component's tree of app.
     * Every component which contains action registered with key equals to command,
     * will be update according to that action.
     * @param command represents type of config. update
     */
    void update(int command);

    /**
     * Selects specified year.
     */
    void selectYear(String yearId);

    /**
     * Returns selected year.
     */
    String getSelectedYear();
}
