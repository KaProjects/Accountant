package org.kaleta.model;

import lombok.Data;

import java.util.HashMap;
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
