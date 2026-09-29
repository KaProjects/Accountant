package org.kaleta.entity.json;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Data
public class FinAssetsConfig
{
    private List<Group> groups = new ArrayList<>();

    public Set<String> getYears()
    {
        Set<String> years = new TreeSet<>();
        for (Group group : groups){
            for (Group.Account account : group.getAccounts()){
                for (Group.Account.Record record : account.getRecords()){
                    years.add(record.getYear());
                }
            }
        }
        return years;
    }

    @Data
    public static class Group
    {
        private String name;

        private List<Account> accounts = new ArrayList<>();

        @Data
        public static class Account
        {
            private String name;

            private List<Record> records = new ArrayList<>();

            @Data
            public static class Record
            {
                private String year;
                private String id;
            }
        }
    }
}
