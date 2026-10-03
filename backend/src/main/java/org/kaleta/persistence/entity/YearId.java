package org.kaleta.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class YearId implements Serializable
{
    @Column(name = "year")
    @NotNull
    private String year;

    @Column(name = "id")
    @NotNull
    private String id;
}
