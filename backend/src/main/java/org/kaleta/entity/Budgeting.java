package org.kaleta.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "Budgeting")
public class Budgeting
{
    @EmbeddedId
    private YearId yearId;

    @Column(name = "name")
    @NotNull
    private String name;

    @Column(name = "debit")
    private String debit;

    @Column(name = "credit")
    private String credit;

    @Column(name = "description")
    private String description;

    @Column(name = "planning")
    private String planning;
}
