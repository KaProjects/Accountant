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
    protected List<SchemaModel.Class> clazz;

    public List<SchemaModel.Class> getClazz() {
        if (clazz == null) {
            clazz = new ArrayList<>();
        }
        return this.clazz;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "group"
    })
    public static class Class {

        protected List<SchemaModel.Class.Group> group;
        @XmlAttribute(name = "id", required = true)
        protected String id ="";
        @XmlAttribute(name = "name", required = true)
        protected String name = "";

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

            protected List<SchemaModel.Class.Group.Account> account;
            @XmlAttribute(name = "id", required = true)
            protected String id = "";
            @XmlAttribute(name = "name", required = true)
            protected String name = "";

            @Override
            public int compareTo(Group o) {
                return Integer.parseInt(this.getId()) - Integer.parseInt(o.getId());
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
                protected String id = "";
                @XmlAttribute(name = "name", required = true)
                protected String name = "";
                @XmlAttribute(name = "type", required = true)
                protected String type = "";

                @Override
                public int compareTo(Account o) {
                    return Integer.parseInt(this.getId()) - Integer.parseInt(o.getId());
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
