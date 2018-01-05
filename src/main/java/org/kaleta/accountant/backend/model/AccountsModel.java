package org.kaleta.accountant.backend.model;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "account"
})
@XmlRootElement(name = "accounts")
public class AccountsModel {

    private List<Account> account;
    @XmlAttribute(name = "year", required = true)
    private String year;

    public AccountsModel() { }

    public AccountsModel(AccountsModel accountsModel) {
        this.setYear(accountsModel.getYear());
        for (AccountsModel.Account acc : accountsModel.getAccount()){
            this.getAccount().add(new AccountsModel.Account(acc));
        }
    }

    public List<Account> getAccount() {
        if (account == null) {
            account = new ArrayList<>();
        }
        return this.account;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String value) {
        this.year = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Account {

        @XmlAttribute(name = "schemaId", required = true)
        String schemaId;
        @XmlAttribute(name = "semanticId", required = true)
        String semanticId;
        @XmlAttribute(name = "name", required = true)
        String name;
        @XmlAttribute(name = "metadata", required = true)
        String metadata = "";

        public Account() { }

        Account(Account acc) {
            this.setName(acc.getName());
            this.setSchemaId(acc.getSchemaId());
            this.setSemanticId(acc.getSemanticId());
            this.setMetadata(acc.getMetadata());
        }

        @Override
        public String toString() {
            return this.getName();
        }

        public String getFullId(){
            return schemaId + "." + semanticId;
        }

        public String getClassId(){
            return schemaId.substring(0,1);
        }

        public String getGroupId(){
            return schemaId.substring(1,2);
        }

        public String getSchemaAccountId(){
            return schemaId.substring(2,3);
        }

        public String getSchemaId() {
            return schemaId;
        }

        public void setSchemaId(String value) {
            this.schemaId = value;
        }

        public String getSemanticId() {
            return semanticId;
        }

        public void setSemanticId(String value) {
            this.semanticId = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String value) {
            this.name = value;
        }

        public String getMetadata() {
            return metadata;
        }

        public void setMetadata(String value) {
            this.metadata = value;
        }
    }
}
