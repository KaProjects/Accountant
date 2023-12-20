package org.kaleta.dto;

import lombok.Data;
import org.kaleta.entity.Account;

import java.util.ArrayList;
import java.util.List;

@Data
public class YearAccountDto
{
    private String schemaId;
    private String semanticId;
    private String name;
    private String metadata;

    private String schemaAccountName;
    private String schemaGroupName;
    private String schemaClassName;

    public static YearAccountDto from(Account account)
    {
        YearAccountDto dto = new YearAccountDto();
        dto.schemaId = account.getAccountId().getSchemaId();
        dto.semanticId = account.getAccountId().getSemanticId();
        dto.name = account.getName();
        dto.metadata = account.getMetadata();
        return dto;
    }

    public static List<YearAccountDto> from(List<Account> accounts)
    {
        List<YearAccountDto> list = new ArrayList<>();
        accounts.forEach(account -> list.add(YearAccountDto.from(account)));
        return list;
    }

    public static YearAccountDto from(String schemaId, String semanticId, String name, String metadata, String schemaAccountName, String schemaGroupName, String schemaClassName)
    {
        YearAccountDto dto = new YearAccountDto();
        dto.setSchemaId(schemaId);
        dto.setSemanticId(semanticId);
        dto.setName(name);
        dto.setMetadata(metadata);
        dto.setSchemaClassName(schemaClassName);
        dto.setSchemaGroupName(schemaGroupName);
        dto.setSchemaAccountName(schemaAccountName);
        return dto;
    }
}
