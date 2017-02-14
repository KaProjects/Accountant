package org.kaleta.accountant.frontend.component.year.model;

/**
 * Created by Stanislav Kaleta on 14.02.2017.
 */
public class AccountModel {

    public boolean groupDeletable(int cId, int gId){
        // if any existing account which schemaId starting with cId,gId has positive turnover -> group cannot be deleted
        // TODO: 2/14/17 impl
        return cId == 0;
    }

    public boolean accountDeletable(int cId, int gId, int aId){
        // if any existing account which schemaId starting with cId,gId,aid has positive turnover -> account cannot be deleted
        // TODO: 2/14/17 impl
        return cId == 0;
    }
}
