package org.kaleta.accountant.frontend.component.year;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Stanislav Kaleta on 09.01.2017.
 */
public class YearModel {
    private int yearIndex;
    private boolean isActive;
    private YearModel.Schema schema;








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

    public Schema getSchema() {
        if (schema == null){
            schema = new Schema();
        }
        return schema;
    }

    public static class Schema{
        private Map<Integer,Clazz> classes = new TreeMap<>();

        public Map<Integer,Clazz> getClasses(){
            return classes;
        }

        public static class Clazz{
            private String name;
            private Map<Integer,Group> groups = new TreeMap<>();

            public Clazz() {}

            public Clazz(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public Map<Integer,Group> getGroups(){
                return groups;
            }

            public static class Group{
                private String name;
                private Map<Integer,Account> accounts = new TreeMap<>();

                public Group() {}

                public Group(String name) {
                    this.name = name;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public Map<Integer,Account> getAccounts(){
                    return accounts;
                }

                public static class Account{
                    private String type;
                    private String name;

                    public Account() {}

                    public Account(String type, String name) {
                        this.type = type;
                        this.name = name;
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
}
