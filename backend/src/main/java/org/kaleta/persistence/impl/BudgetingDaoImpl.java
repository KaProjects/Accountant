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
    public List<Budgeting> getSchema(String year){
        return entityManager.createQuery("SELECT b FROM Budgeting b WHERE b.yearId.year=:year ORDER BY b.yearId.id", Budgeting.class)
                .setParameter("year", year)
                .getResultList();
    }

    @Override
    public void replaceSchema(String year, List<Budgeting> rows)
    {
        entityManager.createQuery("DELETE FROM Budgeting b WHERE b.yearId.year=:year")
                .setParameter("year", year)
                .executeUpdate();
        rows.forEach(entityManager::persist);
    }
}
