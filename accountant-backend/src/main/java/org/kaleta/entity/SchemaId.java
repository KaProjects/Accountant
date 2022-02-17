package org.kaleta.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@Embeddable
public class SchemaId implements Serializable {

    @Column(name = "year")
    @NotNull
    private String year;

    @Column(name = "id")
    @NotNull
    private String id;
}
