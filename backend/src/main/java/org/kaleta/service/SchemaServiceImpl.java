package org.kaleta.service;

import org.kaleta.dao.SchemaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SchemaServiceImpl implements SchemaService{

    @Autowired
    SchemaDao schemaDao;

    @Override
    public String getAccountName(String year, String accountId){
        if (accountId.length() != 3) {
            throw new IllegalArgumentException("Account id length should be 3, but provided id='" + accountId + "'");
        }
        return schemaDao.getNameById(year, accountId);
    }

    @Override
    public String getGroupName(String year, String groupId){
        if (groupId.length() != 2) {
            throw new IllegalArgumentException("Group id length should be 2, but provided id='" + groupId + "'");
        }
        return schemaDao.getNameById(year, groupId);
    }

    @Override
    public String getClassName(String year, String classId){
        if (classId.length() != 1) {
            throw new IllegalArgumentException("Class id length should be 1, but provided id='" + classId + "'");
        }
        return schemaDao.getNameById(year, classId);
    }

}
