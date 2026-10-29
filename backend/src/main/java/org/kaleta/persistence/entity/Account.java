package org.kaleta.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

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
}
