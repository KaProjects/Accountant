package org.kaleta.persistence.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.kaleta.persistence.api.BudgetingDao;
import org.kaleta.persistence.entity.Budgeting;

import java.util.List;

@ApplicationScoped
public class BudgetingDaoImpl implements BudgetingDao
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
