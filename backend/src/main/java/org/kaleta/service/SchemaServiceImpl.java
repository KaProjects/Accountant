package org.kaleta.service;

import org.kaleta.dao.SchemaDao;
import org.kaleta.entity.Schema;
import org.kaleta.model.SchemaClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@Service
public class SchemaServiceImpl implements SchemaService
{
    @Autowired
    SchemaDao schemaDao;

    @Override
    public String getAccountName(String year, String accountId)
    {
        validateIdLength(accountId, 3);
        return schemaDao.getNameById(year, accountId);
    }

    @Override
    public List<Schema> getSchemaAccountsByGroup(String year, String groupId)
    {
        validateIdLength(groupId, 2);
        return schemaDao.getAccountByGroup(year, groupId);
    }

    @Override
    public String getAccountType(String year, String accountId)
    {
        validateIdLength(accountId, 3);
        return schemaDao.getAccountById(year, accountId).getType();
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
                        .getAccounts().put(schema.getYearId().getId(), new SchemaClass.Group.Account(schema.getYearId().getId(), schema.getName(), schema.getType()));
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
                classMap.get(schemaId.substring(0,1)).getGroup(schemaId.substring(0,2)).addAccount(new SchemaClass.Group.Account(schemaId, schema.getName(), schema.getType()));
            }
        }
        return classMap;
    }

    @Override
    public boolean isDebitType(String year, String accountId)
    {
        validateIdLength(accountId, 3);
        return Objects.equals(getAccountType(year, accountId), "A") || Objects.equals(getAccountType(year, accountId), "E");
    }

    @Override
    public boolean isCreditType(String year, String accountId)
    {
        validateIdLength(accountId, 3);
        return Objects.equals(getAccountType(year, accountId), "L") || Objects.equals(getAccountType(year, accountId), "R");
    }

    private void validateIdLength(String id, Integer expected)
    {
        if (id.length() != expected) {
            throw new IllegalArgumentException("Id length should be " + expected + ", but provided id='" + id + "'");
        }
    }
}
