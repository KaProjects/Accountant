package org.kaleta.repository;

import org.kaleta.Constants;
import org.kaleta.entity.Transaction;
import org.kaleta.entity.xml.Transactions;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;

public class TransactionRepositoryImpl implements TransactionRepository
{
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
    public List<Transaction> list(String year, String debitPrefix, String creditPrefix)
    {
        return entityManager.createQuery(selectYearly
                        + " AND t.debit LIKE :debit AND t.credit LIKE :credit", Transaction.class)
                .setParameter("year", year)
                .setParameter("debit", debitPrefix + "%")
                .setParameter("credit", creditPrefix + "%")
                .getResultList();
    }

    @Override
    public List<Transaction> list(String year, String schemaPrefix)
    {
        return entityManager.createQuery(selectYearly
                        + " AND (t.debit LIKE :schema OR t.credit LIKE :schema)", Transaction.class)
                .setParameter("year", year)
                .setParameter("schema", schemaPrefix + "%")
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
    public List<Transaction> listByDescriptionMatching(String year, String descriptionSubString)
    {
        return entityManager.createQuery(selectYearly
                        + " AND t.description LIKE :description"
                        + excludeOffBalanceTransactions, Transaction.class)
                .setParameter("year", year)
                .setParameter("description", "%" + descriptionSubString + "%")
                .getResultList();
    }

    @Override
    public Transaction getInitialTransaction(String year, String accountId, boolean isDebit)
    {
        String accountsCondition = isDebit
                ? " AND t.debit=:accountId AND t.credit=:initialId"
                : " AND t.debit=:initialId AND t.credit=:accountId";

        return entityManager.createQuery(selectYearly + accountsCondition, Transaction.class)
                .setParameter("year", year)
                .setParameter("accountId", accountId)
                .setParameter("initialId", Constants.Account.INIT_ACC_ID)
                .getSingleResult();
    }

    @Override
    public List<Transaction> listBySchema(String year, String schemaId, String month)
    {
        String formattedMonth = month.length() == 1 ? "0" + month : month;

        return entityManager.createQuery(selectYearly
                        + " AND (t.debit LIKE :schemaId OR t.credit LIKE :schemaId)"
                        + " AND t.date LIKE :month"
                        + excludeOffBalanceTransactions, Transaction.class)
                .setParameter("year", year)
                .setParameter("schemaId", schemaId + "%")
                .setParameter("month", "%" + formattedMonth)
                .getResultList();
    }

    @Override
    public List<Transaction> listForClasses2456(String year)
    {
        return entityManager.createQuery(selectYearly
                        + " AND (t.debit LIKE '2%' OR t.debit LIKE '4%' OR t.debit LIKE '5%' OR t.debit LIKE '6%' OR t.credit LIKE '2%' OR t.credit LIKE '4%' OR t.credit LIKE '5%' OR t.credit LIKE '6%')"
                        + excludeOffBalanceTransactions, Transaction.class)
                .setParameter("year", year)
                .getResultList();
    }

    @Override
    public List<Transaction> listClosingTransactions()
    {
        return entityManager.createQuery("SELECT t FROM Transaction t WHERE"
                        + " t.debit=:closing OR t.credit=:closing", Transaction.class)
                .setParameter("closing", Constants.Account.CLOSING_ACC_ID)
                .getResultList();
    }

    @Override
    public List<Transaction> listProfitTransactions()
    {
        return entityManager.createQuery("SELECT t FROM Transaction t WHERE"
                        + " t.debit=:closing OR t.credit=:closing", Transaction.class)
                .setParameter("closing", Constants.Account.PROFIT_ACC_ID)
                .getResultList();
    }

    @Override
    public List<Transaction> listFinancialAssetTransactions(String year)
    {
        return entityManager.createQuery(selectYearly
                        + " AND (t.debit LIKE '23%' OR t.debit LIKE '546%' OR t.credit LIKE '23%' OR t.credit LIKE '546%')", Transaction.class)
                .setParameter("year", year)
                .getResultList();
    }
}
