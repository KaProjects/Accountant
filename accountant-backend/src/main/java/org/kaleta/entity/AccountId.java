package org.kaleta.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@Embeddable
public class AccountId implements Serializable {

    @Column(name = "year")
    @NotNull
    private String year;

    @Column(name = "schema_id")
    @NotNull
    private String schemaId;

    @Column(name = "semantic_id")
    @NotNull
    private String semanticId;
}
