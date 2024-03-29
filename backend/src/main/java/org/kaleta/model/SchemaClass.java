package org.kaleta.model;

import lombok.Data;
import org.kaleta.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Data
public class SchemaClass
{
    private String id;
    private String name;
    private Map<String, Group> groups = new TreeMap<>();

    public SchemaClass(){}
    public SchemaClass(String id, String name){
        this.id = id;
        this.name = name;
    }

    public void addGroup(Group group){
        groups.put(group.getId(), group);
    }
    public Group getGroup(String groupId) {
        return groups.get(groupId);
    }

    public List<String> getGroupSuffixes() {
        List<String> suffixes = new ArrayList<>();
        groups.keySet().forEach(id -> suffixes.add(id.substring(1,2)));
        return suffixes;
    }

    @Data
    public static class Group {
        private String id;
        private String name;
        private Map<String, Account> accounts = new TreeMap<>();

        public Group(String id, String name)
        {
            this.id = id;
            this.name = name;
        }

        public void addAccount(Account account){
            accounts.put(account.getId(), account);
        }

        public Account getAccount(String accountId) {
            return accounts.get(accountId);
        }

        public Account getAccountBySuffix(String accountIdSuffix) {
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
            private Constants.AccountType type;

            public Account(String id, String name, Constants.AccountType type)
            {
                this.id = id;
                this.name = name;
                this.type = type;
            }
        }
    }
}
