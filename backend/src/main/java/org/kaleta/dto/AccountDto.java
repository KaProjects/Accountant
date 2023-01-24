package org.kaleta.dto;

import lombok.Data;
import org.kaleta.entity.Account;

@Data
public class AccountDto
{
    private String year;
    private String schemaId;
    private String semanticId;
    private String name;
    private String metadata;

    private String schemaAccountName;
    private String schemaGroupName;
    private String schemaClassName;

    public AccountDto(){}

    public AccountDto(Account account)
    {
        this.year = account.getAccountId().getYear();
        this.schemaId = account.getAccountId().getSchemaId();
        this.semanticId = account.getAccountId().getSemanticId();
        this.name = account.getName();
        this.metadata = account.getMetadata();
    }
}
