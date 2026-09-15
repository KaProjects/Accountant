package org.kaleta.entity;

import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "ASchema")
public class Schema
{
    @EmbeddedId
    private YearId yearId;

    @Column(name = "name")
    @NotNull
    private String name;

    @Column(name = "type")
    @NotNull
    private String type;
}
