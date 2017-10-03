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

    protected List<Account> account;

    public List<Account> getAccount() {
        if (account == null) {
            account = new ArrayList<>();
        }
        return this.account;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Account {

        @XmlAttribute(name = "schemaId", required = true)
        protected String schemaId;
        @XmlAttribute(name = "semanticId", required = true)
        protected String semanticId;
        @XmlAttribute(name = "type", required = true)
        protected String type;
        @XmlAttribute(name = "name", required = true)
        protected String name;
        @XmlAttribute(name = "metadata", required = true)
        protected String metadata;

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

        public String getType() {
            return type;
        }

        public void setType(String value) {
            this.type = value;
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
