package org.kaleta.accountant.backend.model;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "transaction"
})
@XmlRootElement(name = "transactions")
public class TransactionsModel {

    private List<Transaction> transaction;
    @XmlAttribute(name = "year", required = true)
    private String year;

    public List<Transaction> getTransaction() {
        if (transaction == null) {
            transaction = new ArrayList<>();
        }
        return this.transaction;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String value) {
        this.year = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Transaction {

        @XmlAttribute(name = "id", required = true)
        String id;
        @XmlAttribute(name = "date", required = true)
        String date;
        @XmlAttribute(name = "description", required = true)
        String description;
        @XmlAttribute(name = "amount", required = true)
        String amount;
        @XmlAttribute(name = "debit", required = true)
        String debit;
        @XmlAttribute(name = "credit", required = true)
        String credit;

        public String getId() {
            return id;
        }

        public void setId(String value) {
            this.id = value;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String value) {
            this.date = value;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String value) {
            this.description = value;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String value) {
            this.amount = value;
        }

        public String getDebit() {
            return debit;
        }

        public void setDebit(String value) {
            this.debit = value;
        }

        public String getCredit() {
            return credit;
        }

        public void setCredit(String value) {
            this.credit = value;
        }

    }

}
