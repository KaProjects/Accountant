package org.kaleta.accountant.service;

/**
 * Created by Stanislav Kaleta on 16.04.2016.
 *
 * Puts together logically divided service classes which provide access to data source.
 * Every front-end action should access to back-end via this class.
 */
public class Service {

    /**
     * Instance of configuration's service class.
     */
    public static final ConfigService CONFIG = new ConfigService();

    /**
     * Instance of account's service class
     */
    public static final AccountService ACCOUNT = new AccountService();
}
