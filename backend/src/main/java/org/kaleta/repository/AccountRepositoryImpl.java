package org.kaleta.repository;

import org.kaleta.entity.xml.Accounts;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class AccountRepositoryImpl implements AccountRepository {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public void syncAccounts(Accounts data) {
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
}
