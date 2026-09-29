package org.kaleta.entity;

import lombok.Data;
import org.kaleta.entity.json.FinAssetsConfig;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "Account")
public class Account
{
    @EmbeddedId
    private AccountId accountId;

    @Column(name = "name")
    @NotNull
    private String name;

    @Column(name = "metadata")
    @NotNull
    private String metadata;

    public String getFullId()
    {
        return accountId.getSchemaId() + "." + accountId.getSemanticId();
    }

    public static Account from(FinAssetsConfig.Group.Account.Record config, String name)
    {
        Account account = new Account();
        account.setName(name);
        AccountId accountId = new AccountId();
        accountId.setYear(config.getYear());
        accountId.setSchemaId(config.getId().split("\\.")[0]);
        accountId.setSemanticId(config.getId().split("\\.")[1]);
        account.setAccountId(accountId);
        return account;
    }
}
