package org.kaleta.service;

import java.io.IOException;
import java.util.List;

public interface SyncService
{
    /**
     * Syncs the data of a single year from the desktop app datasource to the backend database.
     * <p>
     * Transactions and accounts are read from the year's own sub-directory, while the schema is
     * a single file shared by all years, kept in the root of the datasource.
     *
     * @param dataSource full path to the data directory that contains config.xml and schema.xml
     * @param year the year to sync, also the name of its sub-directory
     * @return result log of the sync
     * @throws IOException if a data file I/O error occurs
     */
    String sync(String dataSource, String year) throws IOException;

    /**
     * @param dataSource full path to the data directory that contains config.xml file
     * @return list of years from config file
     * @throws IOException if a data file I/O error occurs
     */
    List<String> getYears(String dataSource) throws IOException;

    /**
     *
     * @param dataSource full path to the data directory that contains config.xml file
     * @param year a year to check
     * @return true if the year is active, false otherwise
     * @throws IOException if a data file I/O error occurs
     */
    boolean isActive(String dataSource, String year) throws IOException;

    /**
     * Validates data for specified year.
     * 1. validate transactions (date, accounts exist)
     * 2. validate accounts (schema exists)
     * 3. validate schema (classes and groups exist for account, type set for accounts)
     * 4. for inactive years: for every account (debit sum == credit sum)
     * 5. active year: assets balance = liabilities balance + (revenues balance - expenses balance)
     *
     * @return message from validator
     */
    String validate(String year, boolean isActive);
}
