package org.kaleta.service;

import org.kaleta.dao.SchemaDao;
import org.kaleta.entity.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchemaServiceImpl implements SchemaService{

    @Autowired
    SchemaDao schemaDao;

    @Override
    public String getAccountName(String year, String accountId){
        validateIdLength(accountId, 3);
        return schemaDao.getNameById(year, accountId);
    }

    @Override
    public String getGroupName(String year, String groupId){
        validateIdLength(groupId, 2);
        return schemaDao.getNameById(year, groupId);
    }

    @Override
    public String getClassName(String year, String classId){
        validateIdLength(classId, 1);
        return schemaDao.getNameById(year, classId);
    }

    @Override
    public List<Schema> getSchemaAccountsByGroup(String year, String groupId){
        validateIdLength(groupId, 2);
        return schemaDao.getAccountByGroup(year, groupId);
    }

    @Override
    public String getAccountType(String year, String accountId){
        validateIdLength(accountId, 3);
        return schemaDao.getAccountById(year, accountId).getType();
    }

    private void validateIdLength(String id, Integer expected){
        if (id.length() != expected) {
            throw new IllegalArgumentException("Id length should be " + expected + ", but provided id='" + id + "'");
        }
    }

    @Override
    public List<Schema> list(String year) {
        return schemaDao.list(year);
    }
}
