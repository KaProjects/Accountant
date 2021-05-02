package org.kaleta.accountant.frontend.core.accounting;

import org.kaleta.accountant.common.Utils;
import org.kaleta.accountant.service.Service;

import java.util.Arrays;

public class AccountAggregate {

    private String name = "";
    private String[] increasingSchemaIds = new String[]{};
    private String[] decreasingSchemaIds = new String[]{};

    private AccountAggregate() {
    }

    public static AccountAggregate create(String name) {
        AccountAggregate aggregate = new AccountAggregate();
        aggregate.setName(name);
        return aggregate;
    }

    public AccountAggregate increasing(String... schemaIds) {
        increasingSchemaIds = schemaIds;
        return this;
    }

    public AccountAggregate decreasing(String... schemaIds) {
        decreasingSchemaIds = schemaIds;
        return this;
    }

    public boolean isSingleSchemaId() {
        return decreasingSchemaIds.length == 0 && increasingSchemaIds.length == 1;
    }

    public boolean isExactAccount() {
        return decreasingSchemaIds.length == 0 && increasingSchemaIds.length == 1 && increasingSchemaIds[0].contains(".");
    }

    public boolean isSingleClass() {
        return isSingleSchemaId() && increasingSchemaIds[0].length() == 1;
    }

    public boolean isSingleGroup() {
        return isSingleSchemaId() && increasingSchemaIds[0].length() == 2;
    }

    public boolean isSingleAccount() {
        return isSingleSchemaId() && increasingSchemaIds[0].length() == 3;
    }

    public String getSchemaId() {
        if (!isSingleSchemaId()) throw new IllegalArgumentException("not a single schema ID aggregate");
        return increasingSchemaIds[0];
    }

    public String getClassId() {
        if (!isSingleSchemaId()) throw new IllegalArgumentException("not a single schema ID aggregate");
        if (increasingSchemaIds[0].length() < 1)
            throw new IllegalArgumentException("no class ID for schema ID: " + increasingSchemaIds[0]);
        return increasingSchemaIds[0].substring(0, 1);
    }

    public String getGroupId() {
        if (!isSingleSchemaId()) throw new IllegalArgumentException("not a single schema ID aggregate");
        if (increasingSchemaIds[0].length() < 2)
            throw new IllegalArgumentException("no group ID for schema ID: " + increasingSchemaIds[0]);
        return increasingSchemaIds[0].substring(1, 2);
    }

    public String getAccountId() {
        if (!isSingleSchemaId()) throw new IllegalArgumentException("not a single schema ID aggregate");
        if (increasingSchemaIds[0].length() < 3)
            throw new IllegalArgumentException("no account ID for schema ID: " + increasingSchemaIds[0]);
        return increasingSchemaIds[0].substring(2, 3);
    }


    public Integer[] getMonthlyCumulativeBalance(String year) {
        if (isExactAccount()) {
            return Service.TRANSACTIONS.getAccountMonthlyCumulativeBalance(year, Service.ACCOUNT.getAccount(year, increasingSchemaIds[0]));
        }
        if (decreasingSchemaIds.length == 0) {
            return Service.TRANSACTIONS.getMonthlySchemaIdPrefixCumulativeBalance(year, increasingSchemaIds);
        } else {
            return Utils.substractArrays(
                    Service.TRANSACTIONS.getMonthlySchemaIdPrefixCumulativeBalance(year, increasingSchemaIds),
                    Service.TRANSACTIONS.getMonthlySchemaIdPrefixCumulativeBalance(year, decreasingSchemaIds)
            );
        }
    }

    public Integer[] getMonthlyBalance(String year) {
        if (isExactAccount()) {
            return Service.TRANSACTIONS.getAccountMonthlyBalance(year, Service.ACCOUNT.getAccount(year, increasingSchemaIds[0]));
        }
        if (decreasingSchemaIds.length == 0) {
            return Service.TRANSACTIONS.getMonthlySchemaIdPrefixBalance(year, increasingSchemaIds);
        } else {
            return Utils.substractArrays(
                    Service.TRANSACTIONS.getMonthlySchemaIdPrefixBalance(year, increasingSchemaIds),
                    Service.TRANSACTIONS.getMonthlySchemaIdPrefixBalance(year, decreasingSchemaIds)
            );
        }
    }

    public Integer[] getMonthlyTurnover(String year) {
        if (isExactAccount()) {
            return Service.TRANSACTIONS.getAccountMonthlyTurnover(year, Service.ACCOUNT.getAccount(year, increasingSchemaIds[0]));
        }
        if (decreasingSchemaIds.length == 0) {
            return Service.TRANSACTIONS.getMonthlySchemaIdPrefixTurnover(year, increasingSchemaIds);
        } else {
            return Utils.substractArrays(
                    Service.TRANSACTIONS.getMonthlySchemaIdPrefixTurnover(year, increasingSchemaIds),
                    Service.TRANSACTIONS.getMonthlySchemaIdPrefixTurnover(year, decreasingSchemaIds)
            );
        }
    }

    public Integer getBalance(String year) {
        if (isExactAccount()) {
            return Service.TRANSACTIONS.getAccountBalance(year, Service.ACCOUNT.getAccount(year, increasingSchemaIds[0]));
        }
        if (decreasingSchemaIds.length == 0) {
            return Service.TRANSACTIONS.getSchemaIdPrefixBalance(year, increasingSchemaIds);
        } else {
            return Service.TRANSACTIONS.getSchemaIdPrefixBalance(year, increasingSchemaIds)
                    - Service.TRANSACTIONS.getSchemaIdPrefixBalance(year, decreasingSchemaIds);
        }
    }

    public Integer getInitialValue(String year) {
        if (isExactAccount()) {
            return Service.TRANSACTIONS.getAccountInitialValue(year, Service.ACCOUNT.getAccount(year, increasingSchemaIds[0]));
        }
        if (decreasingSchemaIds.length == 0) {
            return Service.TRANSACTIONS.getSchemaIdPrefixInitialValue(year, increasingSchemaIds);
        } else {
            return Service.TRANSACTIONS.getSchemaIdPrefixInitialValue(year, increasingSchemaIds)
                    - Service.TRANSACTIONS.getSchemaIdPrefixInitialValue(year, decreasingSchemaIds);
        }
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getIncreasingSchemaIds() {
        return increasingSchemaIds;
    }

    public String[] getDecreasingSchemaIds() {
        return decreasingSchemaIds;
    }

    @Override
    public String toString() {
        return "AccountAggregate{name=" + name +
                "\nincreasingSchemaIds=" + Arrays.toString(increasingSchemaIds) +
                "\ndecreasingSchemaIds=" + Arrays.toString(decreasingSchemaIds) +
                "\n}";
    }
}
