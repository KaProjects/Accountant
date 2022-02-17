package org.kaleta.dao;

import org.kaleta.entity.Account;
import org.kaleta.repository.AccountRepository;
import org.springframework.data.repository.CrudRepository;

/**
 * Only to group CrudRepository and AccountRepository.
 */
public interface AccountDao extends CrudRepository<Account, String>, AccountRepository {
}
