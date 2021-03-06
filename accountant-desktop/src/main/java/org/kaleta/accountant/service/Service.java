package org.kaleta.accountant.service;

/**
 * Puts together logically divided service classes which provide access to data source.
 * Every front-end action should access to back-end via this class.
 */
public class Service {

    /**
     * Instance of configuration's service class.
     */
    public static final ConfigService CONFIG = new ConfigService();

    /**
     * Instance of schema's service class.
     */
    public static final SchemaService SCHEMA = new SchemaService();

    /**
     * Instance of account's service class
     */
    public static final AccountsService ACCOUNT = new AccountsService();

    /**
     * Instance of transaction's service class
     */
    public static final TransactionsService TRANSACTIONS = new TransactionsService();

    /**
     * Instance of procedure's service class
     */
    public static final ProceduresService PROCEDURES = new ProceduresService();

    /**
     * Instance of analysis service class
     */
    public static final AnalysisService ANALYSIS = new AnalysisService();

    /**
     * Instance of Firebase service class
     */
    public static final FirebaseService FIREBASE = new FirebaseService();
}
