package org.kaleta.dao;

import org.kaleta.entity.Transaction;
import org.kaleta.repository.TransactionRepository;
import org.springframework.data.repository.CrudRepository;

/**
 * Only to group CrudRepository and TransactionRepository.
 */
public interface TransactionDao extends CrudRepository<Transaction, String>, TransactionRepository {

}