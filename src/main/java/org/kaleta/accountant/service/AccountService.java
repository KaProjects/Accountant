package org.kaleta.accountant.service;

import org.kaleta.accountant.backend.entity.Procedures;
import org.kaleta.accountant.backend.entity.Schema;
import org.kaleta.accountant.backend.entity.Semantic;
import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.backend.manager.jaxb.ProceduresManager;
import org.kaleta.accountant.backend.manager.jaxb.SchemaManager;
import org.kaleta.accountant.backend.manager.jaxb.SemanticManager;
import org.kaleta.accountant.frontend.Initializer;
import org.kaleta.accountant.frontend.common.ErrorDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Stanislav Kaleta on 20.04.2016.
 *
 * todo doc
 */
public class AccountService {

    AccountService(){
        // package-private
    }

    /**
     * todo doc
     */
    public Schema getSchema(){
        try {
            return new SchemaManager().retrieve();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * Returns Schema which contains only accounts with specified account type.
     * @param accountType org.kaleta.accountant.backend.constants.AccountType.*
     */
    public Schema getSchemaForAccountType(String accountType){
        try {
            Schema schema = new Schema();
            Schema schemaComplete = new SchemaManager().retrieve();
            for (Schema.Class clazz : schemaComplete.getClazz()){
                List<Schema.Class.Group> groupMatch = new ArrayList<>();
                for (Schema.Class.Group group : clazz.getGroup()){
                    List<Schema.Class.Group.Account> accountMatch = new ArrayList<>();
                    for (Schema.Class.Group.Account account : group.getAccount()){
                        if (account.getType().equals(accountType)){
                            accountMatch.add(account);
                        }
                    }
                    if (!accountMatch.isEmpty()){
                        Schema.Class.Group matchedGroup = new Schema.Class.Group();
                        matchedGroup.setId(group.getId());
                        matchedGroup.setName(group.getName());
                        matchedGroup.getAccount().addAll(accountMatch);
                        groupMatch.add(matchedGroup);
                    }
                }
                if (!groupMatch.isEmpty()){
                    Schema.Class matchedClass = new Schema.Class();
                    matchedClass.setId(clazz.getId());
                    matchedClass.setName(clazz.getName());
                    matchedClass.getGroup().addAll(groupMatch);
                    schema.getClazz().add(matchedClass);
                }
            }
            return schema;
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * todo doc
     */
    public void setSchema(Schema schema){
        try {
            new SchemaManager().update(schema);
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    public List<Semantic.Account> getSemanticAccounts(String schemaId){
        try {
            Semantic semantic = new SemanticManager().retrieve();
            return semantic.getAccount().stream().filter(account -> account.getSchemaId().equals(schemaId)).collect(Collectors.toList());
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * todo doc
     */
    public Semantic getSemanticAccounts(){
        try {
            return new SemanticManager().retrieve();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * todo doc
     */
    public void setSemanticAccounts(Semantic semantic){
        try {
            new SemanticManager().update(semantic);
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * todo doc
     */
    public void createSemanticAccount(Semantic.Account account){
        // TODO: 5/30/16 impl.
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * todo doc
     */
    public Procedures getProcedures(){
        try {
            return new ProceduresManager().retrieve();
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * todo doc
     */
    public void setProcedures(Procedures procedures){
        try {
            new ProceduresManager().update(procedures);
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * todo doc
     */
    public void createProcedure(Procedures.Procedure procedure){
        try {
            ProceduresManager manager = new ProceduresManager();
            Procedures procedures = manager.retrieve();

            int lastId = 0;
            for (Procedures.Procedure pr : procedures.getProcedure()){
                int prId = Integer.parseInt(pr.getId());
                lastId = (prId > lastId) ? prId : lastId;
            }
            procedure.setId(String.valueOf(lastId + 1));

            procedures.getProcedure().add(procedure);
            manager.update(procedures);
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }


    /**
     * todo doc
     */
    public String getAccountName(String schemaId){
        try {
            final String[] name = new String[]{""};
            Schema schema = new SchemaManager().retrieve();
            schema.getClazz().stream().filter(clazz -> clazz.getId().equals(schemaId.substring(0, 1))).forEach(clazz -> {
                clazz.getGroup().stream().filter(group -> group.getId().equals(schemaId.substring(1, 2))).forEach(group -> {
                    group.getAccount().stream().filter(acc -> acc.getId().equals(schemaId.substring(2, 3))).forEach(acc -> {
                        name[0] = acc.getName();
                    });
                });
            });
            if (name[0].equals("")) {
                throw new IllegalArgumentException("Account with schema id '" + schemaId + "' not found;");
            } else {
                return name[0];
            }
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    public String getAccountFullName(String schemaId, String semanticId){
        try {
            final String[] name = {""};
            Semantic semantic = new SemanticManager().retrieve();
            semantic.getAccount().stream().filter(acc -> acc.getSchemaId().equals(schemaId)).forEach(acc -> {
                if (acc.getId().equals(semanticId)){
                    name[0] = acc.getName();
                }
            });
            return schemaId + "-" + semanticId + " " + getAccountName(schemaId) + " - " + name[0];
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * todo doc
     */
    public String getAccountType(String schemaId){
        try {
            final String[] name = new String[]{""};
            Schema schema = new SchemaManager().retrieve();
            schema.getClazz().stream().filter(clazz -> clazz.getId().equals(schemaId.substring(0, 1))).forEach(clazz -> {
                clazz.getGroup().stream().filter(group -> group.getId().equals(schemaId.substring(1, 2))).forEach(group -> {
                    group.getAccount().stream().filter(acc -> acc.getId().equals(schemaId.substring(2, 3))).forEach(acc -> {
                        name[0] = acc.getType();
                    });
                });
            });
            if (name[0].equals("")) {
                throw new IllegalArgumentException("Account with schema id '" + schemaId + "' not found!");
            } else {
                return name[0];
            }
        } catch (ManagerException e){
            Initializer.LOG.severe(ErrorDialog.getExceptionStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }
}
