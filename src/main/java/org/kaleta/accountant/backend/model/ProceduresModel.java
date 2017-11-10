//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.11.02 at 04:26:09 PM CET 
//


package org.kaleta.accountant.backend.model;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "procedure"
})
@XmlRootElement(name = "procedures")
public class ProceduresModel {

    protected List<Procedure> procedure;
    @XmlAttribute(name = "year", required = true)
    protected String year;

    public List<Procedure> getProcedure() {
        if (procedure == null) {
            procedure = new ArrayList<>();
        }
        return this.procedure;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String value) {
        this.year = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "transaction"
    })
    public static class Procedure {

        @XmlElement(required = true)
        protected List<Transaction> transaction;
        @XmlAttribute(name = "id", required = true)
        protected String id;
        @XmlAttribute(name = "name", required = true)
        protected String name;

        public List<Transaction> getTransaction() {
            if (transaction == null) {
                transaction = new ArrayList<>();
            }
            return this.transaction;
        }

        public String getId() {
            return id;
        }

        public void setId(String value) {
            this.id = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String value) {
            this.name = value;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Transaction {

            @XmlAttribute(name = "description", required = true)
            protected String description;
            @XmlAttribute(name = "amount", required = true)
            protected String amount;
            @XmlAttribute(name = "debit", required = true)
            protected String debit;
            @XmlAttribute(name = "credit", required = true)
            protected String credit;

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
}
