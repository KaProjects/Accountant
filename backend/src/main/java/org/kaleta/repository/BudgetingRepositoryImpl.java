package org.kaleta.repository;

import org.kaleta.entity.Budgeting;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class BudgetingRepositoryImpl implements BudgetingRepository
{
    @PersistenceContext
    EntityManager entityManager;

    @Override
    public Budgeting getSchemaById(String year, String id)
    {
        return entityManager.createQuery("SELECT b FROM Budgeting b WHERE b.yearId.year=:year AND b.yearId.id=:id", Budgeting.class)
                .setParameter("year", year)
                .setParameter("id", id)
                .getSingleResult();
    }

    @Override
    public List<Budgeting> getSchema(String year){
        return entityManager.createQuery("SELECT b FROM Budgeting b WHERE b.yearId.year=:year", Budgeting.class)
                .setParameter("year", year)
                .getResultList();
    }
}
