package org.kaleta.service;


import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.kaleta.dao.AccountDao;
import org.kaleta.dao.SchemaDao;
import org.kaleta.dao.TransactionDao;
import org.kaleta.entity.xml.Accounts;
import org.kaleta.entity.xml.Schema;
import org.kaleta.entity.xml.Transactions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.FileInputStream;
import java.io.IOException;

import static org.kaleta.Utils.inputStreamToString;

@Service
public class SyncServiceImpl implements SyncService {

    @Autowired
    TransactionDao transactionDao;

    @Autowired
    SchemaDao schemaDao;

    @Autowired
    AccountDao accountDao;

    @Transactional
    public String sync(String dataSource) {
        XmlMapper xmlMapper = new XmlMapper();
        StringBuilder sb = new StringBuilder().append("from: " + dataSource + "\n");
        try {
            String transactionsXml = inputStreamToString(new FileInputStream(dataSource + "/transactions.xml"));
            Transactions transactions = xmlMapper.readValue(transactionsXml, Transactions.class);
            transactionDao.syncTransactions(transactions);
            sb.append("year " + transactions.getYear() + " transactions synced: " + transactions.getTransaction().size() + "\n");

            String schemaXml = inputStreamToString(new FileInputStream(dataSource + "/schema.xml"));
            Schema schema = xmlMapper.readValue(schemaXml, Schema.class);
            schemaDao.syncSchema(schema);
            sb.append("year " + schema.getYear() + " schema classes synced: " + schema.getClazz().size() + "\n");

            String accountsXml = inputStreamToString(new FileInputStream(dataSource + "/accounts.xml"));
            Accounts accounts = xmlMapper.readValue(accountsXml, Accounts.class);
            accountDao.syncAccounts(accounts);
            sb.append("year " + accounts.getYear() + " accounts synced: " + accounts.getAccount().size() + "\n");

            return sb.toString();
        } catch (IOException ioe) {
            return ioe.toString();
        }
    }


}
