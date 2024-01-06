package org.kaleta.repository;

import org.kaleta.entity.Account;
import org.kaleta.entity.xml.Accounts;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class AccountRepositoryImpl implements AccountRepository
{
    @PersistenceContext
    EntityManager entityManager;

    private String selectYearly = "SELECT a FROM Account a WHERE a.accountId.year=:year";

    @Override
    public void syncAccounts(Accounts data)
    {
        entityManager.createNativeQuery("DELETE FROM Account WHERE year=?")
                .setParameter(1, data.getYear())
                .executeUpdate();

        for (Accounts.Account account : data.getAccount()) {
            entityManager.createNativeQuery("INSERT INTO Account (year, schema_id, semantic_id, name, metadata) VALUES (?,?,?,?,?)")
                    .setParameter(1, data.getYear())
                    .setParameter(2, account.getSchemaId())
                    .setParameter(3, account.getSemanticId())
                    .setParameter(4, account.getName())
                    .setParameter(5, account.getMetadata())
                    .executeUpdate();
        }
    }

    @Override
    public List<Account> list(String year)
    {
        return entityManager.createQuery(selectYearly, Account.class)
                .setParameter("year", year)
                .getResultList();
    }

    @Override
    public List<Account> list(String year, String schemaPrefix)
    {
        return entityManager.createQuery(selectYearly + " AND a.accountId.schemaId LIKE :schema", Account.class)
                .setParameter("year", year)
                .setParameter("schema", schemaPrefix + "%")
                .getResultList();
    }

    @Override
    public List<Account> listByMetadata(String year, String metadata)
    {
        return entityManager.createQuery(selectYearly + " AND a.metadata LIKE :metadata", Account.class)
                .setParameter("year", year)
                .setParameter("metadata", "%" + metadata + "%")
                .getResultList();
    }
}
