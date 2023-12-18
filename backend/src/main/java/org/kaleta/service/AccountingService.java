package org.kaleta.service;

import org.kaleta.entity.Transaction;
import org.kaleta.model.GroupComponent;

import java.util.List;

public interface AccountingService
{
    GroupComponent getGroupComponent(String year, String groupId);
    GroupComponent getGroupComponent(String year, String groupId, String... accountId);
    List<Transaction> getSchemaTransactions(String year, String schemaId, String month);
}
