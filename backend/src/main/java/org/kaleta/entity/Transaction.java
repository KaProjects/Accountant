package org.kaleta.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "Transaction")
public class Transaction extends AbstractEntity
{
    @Column(name = "year")
    @NotNull
    private String year;

    @Column(name = "date")
    @NotNull
    private String date;

    @Column(name = "description")
    @NotNull
    private String description;

    @Column(name = "amount")
    @NotNull
    private Integer amount;

    @Column(name = "debit")
    @NotNull
    private String debit;

    @Column(name = "credit")
    @NotNull
    private String credit;
}
