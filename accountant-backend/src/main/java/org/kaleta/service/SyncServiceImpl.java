package org.kaleta.service;


import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.kaleta.dao.TransactionDao;
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

    @Transactional
    public String sync(String dataSource) {
        XmlMapper xmlMapper = new XmlMapper();
        StringBuilder sb = new StringBuilder().append("from: " +  dataSource + "\n");
        try {
            String transactionsXml = inputStreamToString(new FileInputStream(dataSource + "/transactions.xml"));
            Transactions transactions = xmlMapper.readValue(transactionsXml, Transactions.class);
            transactionDao.syncTransactions(transactions);
            sb.append("year " + transactions.getYear() + " transactions synced: " + transactions.getTransaction().size() + "\n");

            return sb.toString();
        } catch (IOException ioe) {
            return ioe.toString();
        }
    }


}
