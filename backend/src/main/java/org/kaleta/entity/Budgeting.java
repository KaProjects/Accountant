package org.kaleta.entity;

import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

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
