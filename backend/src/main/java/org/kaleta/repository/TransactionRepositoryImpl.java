package org.kaleta.repository;

import org.kaleta.entity.Transaction;
import org.kaleta.entity.xml.Transactions;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

public class TransactionRepositoryImpl implements TransactionRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void syncTransactions(Transactions data) {
        entityManager.createNativeQuery("DELETE FROM Transaction WHERE year=?")
                .setParameter(1, data.getYear())
                .executeUpdate();

        for (Transactions.Transaction transaction : data.getTransaction()) {
            entityManager.createNativeQuery("INSERT INTO Transaction (id, year, date, description, amount, debit, credit) VALUES (?,?,?,?,?,?,?)")
                    .setParameter(1, UUID.randomUUID().toString())
                    .setParameter(2, data.getYear())
                    .setParameter(3, transaction.getDate())
                    .setParameter(4, transaction.getDescription())
                    .setParameter(5, transaction.getAmount())
                    .setParameter(6, transaction.getDebit())
                    .setParameter(7, transaction.getCredit())
                    .executeUpdate();

        }
    }

    @Override
    public List<Transaction> listMatching(String year, String debitPrefix, String creditPrefix) {
        return entityManager.createQuery("SELECT t FROM Transaction t WHERE t.year=:year AND t.debit LIKE :debit AND t.credit LIKE :credit", Transaction.class)
                .setParameter("year", year)
                .setParameter("debit", debitPrefix + "%")
                .setParameter("credit", creditPrefix + "%")
                .getResultList();
    }
}
