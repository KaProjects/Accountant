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
    public List<Account> list(String year, String schemaIdPrefix)
    {
        return entityManager.createQuery(selectYearly + " AND a.accountId.schemaId LIKE :schemaId", Account.class)
                .setParameter("year", year)
                .setParameter("schemaId", schemaIdPrefix + "%")
                .getResultList();
    }

    @Override
    public Account get(String year, String schemaId, String semanticId)
    {
        return entityManager.createQuery(selectYearly +
                        " AND a.accountId.schemaId=:schemaId" +
                        " AND a.accountId.semanticId=:semanticId", Account.class)
                .setParameter("year", year)
                .setParameter("schemaId", schemaId)
                .setParameter("semanticId", semanticId)
                .getSingleResult();
    }
}
