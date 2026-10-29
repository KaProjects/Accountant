package org.kaleta.accountant.backend.model;

public class StatementTransactionModel {

    private String date;
    private String description;
    private String amount;
    private String debit;
    private String credit;
    private boolean counterSideIsDebit = true;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDebit() {
        return debit;
    }

    public void setDebit(String debit) {
        this.debit = debit;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    /**
     * Which side of the transaction is the one the statement does not already know: the debit for
     * money spent, the credit for money received. The mappings name a debit account, so only a row
     * whose free side is the debit can be taught by the one that was chosen for it.
     */
    public boolean isCounterSideDebit() {
        return counterSideIsDebit;
    }

    public void setCounterSideDebit(boolean counterSideIsDebit) {
        this.counterSideIsDebit = counterSideIsDebit;
    }
}
