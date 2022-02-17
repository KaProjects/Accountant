package org.kaleta.repository;

import org.kaleta.entity.xml.Schema;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class SchemaRepositoryImpl implements SchemaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void syncSchema(Schema data) {
        entityManager.createNativeQuery("DELETE FROM ASchema WHERE year=?")
                .setParameter(1, data.getYear())
                .executeUpdate();

        for (Schema.Clazz clazz : data.getClazz()) {
            entityManager.createNativeQuery("INSERT INTO ASchema (year, id, name, type) VALUES (?,?,?,?)")
                    .setParameter(1, data.getYear())
                    .setParameter(2, clazz.getId())
                    .setParameter(3, clazz.getName())
                    .setParameter(4, "")
                    .executeUpdate();

            for (Schema.Clazz.Group group : clazz.getGroup()) {
                entityManager.createNativeQuery("INSERT INTO ASchema (year, id, name, type) VALUES (?,?,?,?)")
                        .setParameter(1, data.getYear())
                        .setParameter(2, clazz.getId() + group.getId())
                        .setParameter(3, group.getName())
                        .setParameter(4, "")
                        .executeUpdate();

                for (Schema.Clazz.Group.Account account : group.getAccount()) {
                    entityManager.createNativeQuery("INSERT INTO ASchema (year, id, name, type) VALUES (?,?,?,?)")
                            .setParameter(1, data.getYear())
                            .setParameter(2, clazz.getId() + group.getId() + account.getId())
                            .setParameter(3, account.getName())
                            .setParameter(4, account.getType())
                            .executeUpdate();
                }
            }
        }
    }
}
