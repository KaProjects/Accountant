package org.kaleta.service;

import java.io.IOException;
import java.util.List;

public interface SyncService
{
    /**
     * Syncs all data from desktop app datasource to the backend database.
     *
     * @param dataSource full path to the directory containing single year data (xml files)
     * @return result log of the sync
     * @throws IOException if a data file I/O error occurs
     */
    String sync(String dataSource) throws IOException;

    /**
     * @param dataSource full path to the data directory that contains config.xml file
     * @return list of years from config file
     * @throws IOException if a data file I/O error occurs
     */
    List<String> getYears(String dataSource) throws IOException;
}
