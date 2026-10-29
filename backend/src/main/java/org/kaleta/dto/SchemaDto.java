package org.kaleta.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import org.kaleta.persistence.entity.Schema;

@Data
@RegisterForReflection
public class SchemaDto
{
    private String year;
    private String id;
    private String name;
    private String type;

    public SchemaDto() {}

    public SchemaDto(Schema schema)
    {
        this.year = schema.getYearId().getYear();
        this.id = schema.getYearId().getId();
        this.name = schema.getName();
        this.type = schema.getType();
    }
}
