package org.kaleta.accountant.frontend.component.year;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stanislav Kaleta on 09.01.2017.
 */
@Deprecated
public class YearModel {
    private int yearIndex;
    private List<Account> accounts;

    public int getYearIndex() {
        return yearIndex;
    }

    public void setYearIndex(int yearIndex) {
        this.yearIndex = yearIndex;
    }



    public List<Account> getAccounts() {
        if (accounts == null) {
            accounts = new ArrayList<>();
        }
        return accounts;
    }

    public List<Account> getAccountsById(Integer classId, Integer groupId, Integer accId){
        return null; // // TODO: 1/10/17 asd
    }


    public static class Account {
        private String schemaId;
        private String semanticId;
        private String type;
        private String name;
        // private List<Transaction> transactions;


        public Integer getInitialValue() {
            return 0; // TODO: 1/10/17 (if A) get transaction schemaId.semanticId/711
        }

        public Integer getFinalValue() {
            return 0; // TODO: 1/10/17 (if A) get transaction 721/schemaId.semanticId or debit - credit
        }

        public Integer getTurnover() {
            return 0; // TODO: 1/10/17 (if A) get debit sum
        }

        public String getSchemaId() {
            return schemaId;
        }

        public void setSchemaId(String schemaId) {
            this.schemaId = schemaId;
        }

        public String getSemanticId() {
            return semanticId;
        }

        public void setSemanticId(String semanticId) {
            this.semanticId = semanticId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
