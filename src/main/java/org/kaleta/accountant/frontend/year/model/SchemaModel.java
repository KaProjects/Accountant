package org.kaleta.accountant.frontend.year.model;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Stanislav Kaleta on 14.02.2017.
 */
@Deprecated
public class SchemaModel {
    private Map<Integer, Clazz> classes = new TreeMap<>();

    public Map<Integer, Clazz> getClasses() {
        return classes;
    }

    public String getClassName(String schemaId) {
        return classes.get(Integer.parseInt(schemaId.substring(0, 1))).getName();
    }

    public String getGroupName(String schemaId) {
        return classes.get(Integer.parseInt(schemaId.substring(0, 1)))
                .getGroups().get(Integer.parseInt(schemaId.substring(1, 2))).getName();
    }

    public String getAccName(String schemaId) {
        return classes.get(Integer.parseInt(schemaId.substring(0, 1)))
                .getGroups().get(Integer.parseInt(schemaId.substring(1, 2)))
                .getAccounts().get(Integer.parseInt(schemaId.substring(2, 3))).getName();
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
}