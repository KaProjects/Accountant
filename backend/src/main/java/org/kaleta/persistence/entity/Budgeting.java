package org.kaleta.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

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
