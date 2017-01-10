package org.kaleta.accountant.frontend.component.year;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Stanislav Kaleta on 09.01.2017.
 */
public class YearModel {
    private int yearIndex;
    private boolean isActive;
    private Map<Integer, Clazz> classes;
    private List<Account> accounts;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getYearIndex() {
        return yearIndex;
    }

    public void setYearIndex(int yearIndex) {
        this.yearIndex = yearIndex;
    }

    public Map<Integer, Clazz> getClasses() {
        if (classes == null) {
            classes = new TreeMap<>();
        }
        return classes;
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


    public static class Clazz {
        private Integer id;
        private String name;
        private Map<Integer, Group> groups = new TreeMap<>();

        public Clazz() {
        }

        public Clazz(Integer id, String name) {
            this.id =id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<Integer, Group> getGroups() {
            return groups;
        }

        public static class Group {
            private Integer id;
            private String name;
            private Map<Integer, Account> accounts = new TreeMap<>();

            public Group() {
            }

            public Group(Integer id, String name) {
                this.id =id;
                this.name = name;
            }

            public Integer getId() {
                return id;
            }

            public void setId(Integer id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public Map<Integer, Account> getAccounts() {
                return accounts;
            }

            public static class Account {
                private Integer id;
                private String type;
                private String name;

                public Account() {
                }

                public Account(Integer id, String type,String name) {
                    this.id =id;
                    this.type = type;
                    this.name = name;
                }

                public Integer getId() {
                    return id;
                }

                public void setId(Integer id) {
                    this.id = id;
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
