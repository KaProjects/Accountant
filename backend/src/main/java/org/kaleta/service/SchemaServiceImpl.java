package org.kaleta.service;

import org.kaleta.Constants;
import org.kaleta.dao.SchemaDao;
import org.kaleta.entity.Schema;
import org.kaleta.model.SchemaClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class SchemaServiceImpl implements SchemaService
{
    private final SchemaDao schemaDao;

    @Autowired
    public SchemaServiceImpl(SchemaDao schemaDao)
    {
        this.schemaDao = schemaDao;
    }

    @Override
    public Map<String, String> getSchemaNames(String year)
    {
        Map<String, String> map = new HashMap<>();
        for (Schema schema : schemaDao.list(year))
        {
            map.put(schema.getYearId().getId(), schema.getName());
        }
        return map;
    }

    @Override
    public Map<String, String> getLatestSchemaNames()
    {
        Map<String, String> map = new HashMap<>();
        for (Schema schema : schemaDao.listLatest())
        {
            map.put(schema.getYearId().getId(), schema.getName());
        }
        return map;
    }

    @Override
    public Constants.AccountType getAccountType(String year, String accountId)
    {
        validateIdLength(accountId, 3);
        return Constants.AccountType.valueOf(schemaDao.getAccountById(year, accountId).getType());
    }

    @Override
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

    @Override
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

    @Override
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
