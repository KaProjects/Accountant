package org.kaleta.service;

import org.kaleta.entity.Transaction;
import org.kaleta.model.AccountingData;
import org.kaleta.model.GroupComponent;

import java.util.List;

public interface AccountingService
{
    @Deprecated
    GroupComponent getGroupComponent(String year, String groupId);
    @Deprecated
    GroupComponent getGroupComponent(String year, String groupId, String... accountId);

    AccountingData getCashFlowData(String year);

    List<Transaction> getSchemaTransactions(String year, String schemaId, String month);
}
