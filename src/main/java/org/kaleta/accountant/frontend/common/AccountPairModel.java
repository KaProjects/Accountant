package org.kaleta.accountant.frontend.common;

public class AccountPairModel {
    private String debit;
    private String credit;

    public AccountPairModel(String debit, String credit) {
        this.debit = debit;
        this.credit = credit;
    }

    public String getDebit() {
        return debit;
    }

    public String getCredit() {
        return credit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountPairModel that = (AccountPairModel) o;

        if (!debit.equals(that.debit)) return false;
        return credit.equals(that.credit);
    }

    @Override
    public int hashCode() {
        int result = debit.hashCode();
        result = 31 * result + credit.hashCode();
        return result;
    }
}
