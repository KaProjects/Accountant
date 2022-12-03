package org.kaleta.accountant.data;

import java.util.Objects;

public class Template {

    private String description;
    private String amount;
    private String debit;
    private String credit;

    private String name;

    public Template(){
       // default
    }
    public Template(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Template template = (Template) o;
        return Objects.equals(description, template.description) && Objects.equals(amount, template.amount) && Objects.equals(debit, template.debit) && Objects.equals(credit, template.credit) && Objects.equals(name, template.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, amount, debit, credit, name);
    }

    @Override
    public String toString() {
        return name;
    }
}
