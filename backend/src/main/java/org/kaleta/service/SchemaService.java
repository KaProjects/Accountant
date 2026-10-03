package org.kaleta.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.kaleta.Constants;
import org.kaleta.model.SchemaClass;
import org.kaleta.persistence.api.SchemaDao;
import org.kaleta.persistence.entity.Schema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@ApplicationScoped
public class SchemaService
{
    private final SchemaDao schemaDao;

    @Inject
    public SchemaService(SchemaDao schemaDao)
    {
        this.schemaDao = schemaDao;
    }

    /**
     * @return names of all schema elements for specified year
     */
    public Map<String, String> getSchemaNames(String year)
    {
        Map<String, String> map = new HashMap<>();
        for (Schema schema : schemaDao.list(year))
        {
            map.put(schema.getYearId().getId(), schema.getName());
        }
        return map;
    }

    /**
     * @return names of all schema elements of the latest year
     */
    public Map<String, String> getLatestSchemaNames()
    {
        Map<String, String> map = new HashMap<>();
        for (Schema schema : schemaDao.listLatest())
        {
            map.put(schema.getYearId().getId(), schema.getName());
        }
        return map;
    }

    public Constants.AccountType getAccountType(String year, String accountId)
    {
        validateIdLength(accountId, 3);
        return Constants.AccountType.valueOf(schemaDao.getAccountById(year, accountId).getType());
    }

    /**
     * @return class model specified by class ID and year
     */
    public SchemaClass getClass(String year, String classId)
    {
        validateIdLength(classId, 1);

        SchemaClass clazz = new SchemaClass();
        List<Schema> schemas = schemaDao.list(year, classId);

        for (Schema schema : schemas){
            if (schema.getYearId().getId().length() == 1){
                clazz.setId(schema.getYearId().getId());
                clazz.setName(schema.getName());
            }
            if (schema.getYearId().getId().length() == 2){
                clazz.getGroups().put(schema.getYearId().getId(), new SchemaClass.Group(schema.getYearId().getId(), schema.getName()));
            }
        }
        for (Schema schema : schemas){
            if (schema.getYearId().getId().length() == 3){
                clazz.getGroups().get(schema.getYearId().getId().substring(0,2))
                        .getAccounts().put(schema.getYearId().getId(), new SchemaClass.Group.Account(schema.getYearId().getId(), schema.getName(), Constants.AccountType.valueOf(schema.getType())));
            }
        }
        return clazz;
    }

    /**
     * @return models of all classes for specified year
     */
    public Map<String, SchemaClass> getSchema(String year)
    {
        Map<String, SchemaClass> classMap = new TreeMap<>();
        List<Schema> schemas = schemaDao.list(year);

        for (Schema schema : schemas){
            String schemaId = schema.getYearId().getId();
            if (schemaId.length() == 1){
                classMap.put(schemaId, new SchemaClass(schemaId, schema.getName()));
            }
        }
        for (Schema schema : schemas){
            String schemaId = schema.getYearId().getId();
            if (schemaId.length() == 2){
                classMap.get(schemaId.substring(0,1)).addGroup(new SchemaClass.Group(schemaId, schema.getName()));
            }
        }
        for (Schema schema : schemas){
            String schemaId = schema.getYearId().getId();
            if (schemaId.length() == 3){
                classMap.get(schemaId.substring(0,1)).getGroup(schemaId.substring(0,2)).addAccount(new SchemaClass.Group.Account(schemaId, schema.getName(), Constants.AccountType.valueOf(schema.getType())));
            }
        }
        return classMap;
    }

    /**
     * @return all years from data
     */
    public List<String> getYears()
    {
        return schemaDao.getYears();
    }

    private void validateIdLength(String id, Integer expected)
    {
        if (id.length() != expected) {
            throw new IllegalArgumentException("Id length should be " + expected + ", but provided id='" + id + "'");
        }
    }
}
