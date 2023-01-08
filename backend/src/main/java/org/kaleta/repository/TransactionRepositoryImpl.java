package org.kaleta.repository;

import org.kaleta.entity.Transaction;
import org.kaleta.entity.xml.Transactions;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;

public class TransactionRepositoryImpl implements TransactionRepository {

    @PersistenceContext
    EntityManager entityManager;

    private String selectYearly = "SELECT t FROM Transaction t WHERE t.year=:year";
    private String excludeOffBalanceTransactions = " AND NOT debit LIKE '7%' AND NOT credit LIKE '7%'";

    @Override
    public void syncTransactions(Transactions data)
    {
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
    public List<Transaction> listMatching(String year, String debitPrefix, String creditPrefix)
    {
        return entityManager.createQuery(selectYearly
                        + " AND t.debit LIKE :debit AND t.credit LIKE :credit"
                        + excludeOffBalanceTransactions, Transaction.class)
                .setParameter("year", year)
                .setParameter("debit", debitPrefix + "%")
                .setParameter("credit", creditPrefix + "%")
                .getResultList();
    }

    @Override
    public List<Transaction> listByAccounts(String year, String debit, String credit, String description)
    {
        String debitCondition = "";
        if (debit != null && !debit.isEmpty()){
            if (debit.contains("%")){
                debitCondition = " AND t.debit LIKE :debit";
            } else {
                debitCondition = " AND t.debit=:debit";
            }
        }

        String creditCondition = "";
        if (credit != null && !credit.isEmpty()){
            if (credit.contains("%")){
                creditCondition = " AND t.credit LIKE :credit";
            } else {
                creditCondition = " AND t.credit=:credit";
            }
        }

        String descriptionCondition = "";
        if (description != null && !description.isEmpty()){
            if (description.startsWith("!")){
                description = description.replace("!", "");
                descriptionCondition = " AND t.description NOT LIKE :description";
            } else {
                descriptionCondition = " AND t.description LIKE :description";

            }
        }

        TypedQuery<Transaction> query = entityManager.createQuery(selectYearly
                        + debitCondition
                        + creditCondition
                        + descriptionCondition
                        + excludeOffBalanceTransactions, Transaction.class)
                .setParameter("year", year);

        if (!debitCondition.isEmpty()){
            query.setParameter("debit", debit);
        }
        if (!creditCondition.isEmpty()){
            query.setParameter("credit", credit);
        }
        if (!descriptionCondition.isEmpty()){
            query.setParameter("description", "%" + description + "%");
        }

        return query.getResultList();
    }

    @Override
    public List<Transaction> listByAccount(String year, String account)
    {
        return entityManager.createQuery(selectYearly
                        + " AND (t.debit=:account OR t.credit=:account)"
                        + excludeOffBalanceTransactions, Transaction.class)
                .setParameter("year", year)
                .setParameter("account", account)
                .getResultList();
    }

    @Override
    public List<Transaction> listByDescriptionMatching(String year, String descriptionSubString) {
        return entityManager.createQuery(selectYearly
                        + " AND t.description LIKE :description"
                        + excludeOffBalanceTransactions, Transaction.class)
                .setParameter("year", year)
                .setParameter("description", "%" + descriptionSubString + "%")
                .getResultList();
    }
}
