package org.kaleta.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class SchemaClass
{
    private String id;
    private String name;
    private Map<String, Group> groups = new HashMap<>();

    public Group getGroup(String groupId) {
        return groups.get(groupId);
    }

    @Data
    public static class Group {
        private String id;
        private String name;
        private Map<String, Account> accounts = new HashMap<>();

        public Group(String id, String name)
        {
            this.id = id;
            this.name = name;
        }

        public Account getAccount(String accountIdSuffix) {
            return accounts.get(id + accountIdSuffix);
        }

        public List<String> getAccountSuffixes() {
            List<String> suffixes = new ArrayList<>();
            accounts.keySet().forEach(id -> suffixes.add(id.substring(2,3)));
            return suffixes;
        }

        @Data
        public static class Account {
            private String id;
            private String name;
            private String type;

            public Account(String id, String name, String type)
            {
                this.id = id;
                this.name = name;
                this.type = type;
            }
        }
    }
}
