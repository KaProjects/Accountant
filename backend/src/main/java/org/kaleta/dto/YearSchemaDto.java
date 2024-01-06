package org.kaleta.dto;

import lombok.Data;
import org.kaleta.Constants;
import org.kaleta.model.SchemaClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class YearSchemaDto
{
    private List<Class> classes = new ArrayList<>();

    @Data
    public static class Class
    {
        private String id;
        private String name;
        private List<Group> groups = new ArrayList<>();

        @Data
        public static class Group
        {
            private String id;
            private String name;
            private List<Account> accounts = new ArrayList<>();

            @Data
            public static class Account
            {
                private String id;
                private String name;
                private Constants.AccountType type;
            }
        }
    }

    public static YearSchemaDto from(Map<String, SchemaClass> schemaModels)
    {
        YearSchemaDto dto = new YearSchemaDto();
        for (SchemaClass schemaClass : schemaModels.values())
        {
            Class clazz = new Class();
            clazz.setId(schemaClass.getId());
            clazz.setName(schemaClass.getName());
            for (SchemaClass.Group schemaGroup : schemaClass.getGroups().values())
            {
                Class.Group group = new Class.Group();
                group.setId(schemaGroup.getId());
                group.setName(schemaGroup.getName());
                for (SchemaClass.Group.Account schemaAccount : schemaGroup.getAccounts().values())
                {
                    Class.Group.Account account = new Class.Group.Account();
                    account.setId(schemaAccount.getId());
                    account.setName(schemaAccount.getName());
                    account.setType(schemaAccount.getType());
                    group.getAccounts().add(account);
                }
                clazz.getGroups().add(group);
            }
            dto.getClasses().add(clazz);
        }
        return dto;
    }
}
