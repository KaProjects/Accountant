package org.kaleta.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "Account")
public class Account {

    @EmbeddedId
    private AccountId accountId;

    @Column(name = "name")
    @NotNull
    private String name;

    @Column(name = "metadata")
    @NotNull
    private String metadata;
}
