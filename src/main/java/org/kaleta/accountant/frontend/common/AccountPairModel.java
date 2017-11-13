package org.kaleta.accountant.frontend.common;

public class AccountPairModel {
    private final String debit;
    private final String credit;

    public AccountPairModel(String debit, String credit) {
        this.debit = debit;
        this.credit = credit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountPairModel that = (AccountPairModel) o;

        return debit.equals(that.debit) && credit.equals(that.credit);
    }

    @Override
    public int hashCode() {
        int result = debit.hashCode();
        result = 31 * result + credit.hashCode();
        return result;
    }
}
