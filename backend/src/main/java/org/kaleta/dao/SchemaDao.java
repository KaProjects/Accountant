package org.kaleta.dao;

import org.kaleta.entity.Schema;
import org.kaleta.repository.SchemaRepository;
import org.springframework.data.repository.CrudRepository;

/**
 * Only to group CrudRepository and SchemaRepository.
 */
public interface SchemaDao extends CrudRepository<Schema, String>, SchemaRepository {
}
