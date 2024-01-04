package org.kaleta.repository;

import org.kaleta.entity.Schema;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class SchemaRepositoryImpl implements SchemaRepository
{
    @PersistenceContext
    EntityManager entityManager;

    private String selectYearly = "SELECT s FROM Schema s WHERE s.yearId.year=:year";

    @Override
    public void syncSchema(org.kaleta.entity.xml.Schema data)
    {
        entityManager.createNativeQuery("DELETE FROM ASchema WHERE year=?")
                .setParameter(1, data.getYear())
                .executeUpdate();

        for (org.kaleta.entity.xml.Schema.Clazz clazz : data.getClazz()) {
            entityManager.createNativeQuery("INSERT INTO ASchema (year, id, name, type) VALUES (?,?,?,?)")
                    .setParameter(1, data.getYear())
                    .setParameter(2, clazz.getId())
                    .setParameter(3, clazz.getName())
                    .setParameter(4, "")
                    .executeUpdate();

            for (org.kaleta.entity.xml.Schema.Clazz.Group group : clazz.getGroup()) {
                entityManager.createNativeQuery("INSERT INTO ASchema (year, id, name, type) VALUES (?,?,?,?)")
                        .setParameter(1, data.getYear())
                        .setParameter(2, clazz.getId() + group.getId())
                        .setParameter(3, group.getName())
                        .setParameter(4, "")
                        .executeUpdate();

                for (org.kaleta.entity.xml.Schema.Clazz.Group.Account account : group.getAccount()) {
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

    @Override
    public String getNameById(String year, String id)
    {
        return (String) entityManager.createQuery("SELECT s.name FROM Schema s " +
                        "WHERE s.yearId.year=:year AND s.yearId.id=:id")
                .setParameter("year", year)
                .setParameter("id", id)
                .getSingleResult();
    }

    @Override
    public List<Schema> getAccountByGroup(String year, String groupId)
    {
        return entityManager.createQuery(selectYearly
                        + " AND s.yearId.id LIKE :id AND length(s.yearId.id) = 3", Schema.class)
                .setParameter("year", year)
                .setParameter("id", groupId + "%")
                .getResultList();
    }

    @Override
    public Schema getAccountById(String year, String accountId)
    {
        return entityManager.createQuery(selectYearly + " AND s.yearId.id=:id", Schema.class)
                .setParameter("year", year)
                .setParameter("id", accountId)
                .getSingleResult();
    }

    @Override
    public List<Schema> list(String year)
    {
        return entityManager.createQuery(selectYearly, Schema.class)
                .setParameter("year", year)
                .getResultList();
    }

    @Override
    public List<Schema> list(String year, String idPrefix)
    {
        return entityManager.createQuery(selectYearly + " AND s.yearId.id LIKE :id", Schema.class)
                .setParameter("year", year)
                .setParameter("id", idPrefix + "%")
                .getResultList();
    }

    @Override
    public List<Schema> listLatest()
    {
        return entityManager.createQuery("SELECT s FROM Schema s WHERE s.yearId.year=(SELECT MAX(yearId.year) FROM Schema)", Schema.class)
                .getResultList();
    }

    @Override
    public List<String> getYears()
    {
        return entityManager.createQuery("SELECT DISTINCT s.yearId.year FROM Schema s", String.class)
                .getResultList();
    }
}
