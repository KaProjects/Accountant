package org.kaleta.accountant.backend.model;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "clazz"
})
@XmlRootElement(name = "schema")
public class SchemaModel {

    @XmlElement(name = "class", required = true)
    private List<SchemaModel.Class> clazz;
    @XmlAttribute(name = "year", required = true)
    private String year;

    public SchemaModel(){}

    public SchemaModel(SchemaModel schemaModel){
        this.setYear(schemaModel.getYear());
        for (SchemaModel.Class clazz : schemaModel.getClazz()){
            this.getClazz().add(new SchemaModel.Class(clazz));
        }
    }

    public List<SchemaModel.Class> getClazz() {
        if (clazz == null) {
            clazz = new ArrayList<>();
        }
        return this.clazz;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String value) {
        this.year = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "group"
    })
    public static class Class {

        List<SchemaModel.Class.Group> group;
        @XmlAttribute(name = "id", required = true)
        String id ="";
        @XmlAttribute(name = "name", required = true)
        String name = "";

        public Class(){}

        Class(Class clazz){
            this.setId(clazz.getId());
            this.setName(clazz.getName());
            for (SchemaModel.Class.Group group : clazz.getGroup()){
                this.getGroup().add(new SchemaModel.Class.Group(group));
            }
        }

        @Override
        public String toString() {
            return name;
        }

        public List<SchemaModel.Class.Group> getGroup() {
            if (group == null) {
                group = new ArrayList<>();
            }
            Collections.sort(group);
            return this.group;
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
        @XmlType(name = "", propOrder = {
            "account"
        })
        public static class Group implements Comparable<Group>{

            List<SchemaModel.Class.Group.Account> account;
            @XmlAttribute(name = "id", required = true)
            String id = "";
            @XmlAttribute(name = "name", required = true)
            String name = "";

            public Group(){}

            Group(Group group){
                this.setId(group.getId());
                this.setName(group.getName());
                for (SchemaModel.Class.Group.Account acc : group.getAccount()){
                    this.getAccount().add(new SchemaModel.Class.Group.Account(acc));
                }
            }

            @Override
            public int compareTo(Group o) {
                return Integer.parseInt(this.getId()) - Integer.parseInt(o.getId());
            }

            @Override
            public String toString() {
                return name;
            }

            public List<SchemaModel.Class.Group.Account> getAccount() {
                if (account == null) {
                    account = new ArrayList<>();
                }
                Collections.sort(account);
                return this.account;
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
            public static class Account implements Comparable<Account> {

                @XmlAttribute(name = "id", required = true)
                String id = "";
                @XmlAttribute(name = "name", required = true)
                String name = "";
                @XmlAttribute(name = "type", required = true)
                String type = "";

                public Account(){}

                Account(Account acc){
                    this.setId(acc.getId());
                    this.setName(acc.getName());
                    this.setType(acc.getType());
                }

                @Override
                public int compareTo(Account o) {
                    return Integer.parseInt(this.getId()) - Integer.parseInt(o.getId());
                }

                @Override
                public String toString() {
                    return name;
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

                public String getType() {
                    return type;
                }

                public void setType(String value) {
                    this.type = value;
                }

            }

        }

    }

}
