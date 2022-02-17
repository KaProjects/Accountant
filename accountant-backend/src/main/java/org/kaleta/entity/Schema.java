package org.kaleta.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "ASchema")
public class Schema {

    @EmbeddedId
    private SchemaId schemaId;

    @Column(name = "name")
    @NotNull
    private String name;

    @Column(name = "type")
    @NotNull
    private String type;
}
