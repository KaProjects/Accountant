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
    public List<Budgeting> getSchemaByIdPrefix(String year, String idPrefix)
    {
        return entityManager.createQuery("SELECT b FROM Budgeting b WHERE b.yearId.year=:year AND b.yearId.id LIKE :id", Budgeting.class)
                .setParameter("year", year)
                .setParameter("id", idPrefix + "%")
                .getResultList();
    }
}
