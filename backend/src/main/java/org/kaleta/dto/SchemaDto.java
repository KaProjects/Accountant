package org.kaleta.dto;

import lombok.Data;
import org.kaleta.entity.Schema;

@Data
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
