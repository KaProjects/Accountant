package org.kaleta.accountant.backend.manager;

import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Initializer;
import org.xml.sax.helpers.DefaultHandler;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.SchemaFactory;
import java.io.File;

import static org.kaleta.accountant.common.Constants.Schema.*;

public class SchemaManager implements Manager<SchemaModel> {
    private final String schemaUri;
    private final String schemaFileUri;

    public SchemaManager(String year) {
        schemaUri = "/schema/schema.xsd";
        schemaFileUri = Initializer.DATA_SOURCE + year + File.separator + "schema.xml";
    }

    @Override
    public void create() throws ManagerException {
        SchemaModel newSchemaModel = new SchemaModel();

        SchemaModel.Class c0 = new SchemaModel.Class();
        c0.setId("0");
        c0.setName(CLASS_0_NAME);
        SchemaModel.Class.Group c0AccumulatedDepreciationGroup = new SchemaModel.Class.Group();
        c0AccumulatedDepreciationGroup.setId(ACCUMULATED_DEP_GROUP_ID);
        c0AccumulatedDepreciationGroup.setName(ACCUMULATED_DEP_GROUP_NAME);
        c0.getGroup().add(c0AccumulatedDepreciationGroup);
        newSchemaModel.getClazz().add(c0);

        SchemaModel.Class c1 = new SchemaModel.Class();
        c1.setId("1");
        c1.setName(CLASS_1_NAME);
        newSchemaModel.getClazz().add(c1);

        SchemaModel.Class c2 = new SchemaModel.Class();
        c2.setId("2");
        c2.setName(CLASS_2_NAME);
        newSchemaModel.getClazz().add(c2);

        SchemaModel.Class c3 = new SchemaModel.Class();
        c3.setId("3");
        c3.setName(CLASS_3_NAME);
        SchemaModel.Class.Group c3ReceivablesGroup = new SchemaModel.Class.Group();
        c3ReceivablesGroup.setId(RECEIVABLES_GROUP_ID);
        c3ReceivablesGroup.setName(RECEIVABLES_GROUP_NAME);
        SchemaModel.Class.Group.Account c300Acc = new SchemaModel.Class.Group.Account();
        c300Acc.setId(RECEIVABLES_PERSONAL_ACCOUNT_ID);
        c300Acc.setName(RECEIVABLES_PERSONAL_ACCOUNT_NAME);
        c300Acc.setType(Constants.AccountType.ASSET);
        c3ReceivablesGroup.getAccount().add(c300Acc);
        SchemaModel.Class.Group.Account c301Acc = new SchemaModel.Class.Group.Account();
        c301Acc.setId(RECEIVABLES_INSTITUTION_ACCOUNT_ID);
        c301Acc.setName(RECEIVABLES_INSTITUTION_ACCOUNT_NAME);
        c301Acc.setType(Constants.AccountType.ASSET);
        c3ReceivablesGroup.getAccount().add(c301Acc);
        SchemaModel.Class.Group.Account c302Acc = new SchemaModel.Class.Group.Account();
        c302Acc.setId(RECEIVABLES_COMPANY_ACCOUNT_ID);
        c302Acc.setName(RECEIVABLES_COMPANY_ACCOUNT_NAME);
        c302Acc.setType(Constants.AccountType.ASSET);
        c3ReceivablesGroup.getAccount().add(c302Acc);
        c3.getGroup().add(c3ReceivablesGroup);
        SchemaModel.Class.Group c3LiabilitiesGroup = new SchemaModel.Class.Group();
        c3LiabilitiesGroup.setId(LIABILITIES_GROUP_ID);
        c3LiabilitiesGroup.setName(LIABILITIES_GROUP_NAME);
        SchemaModel.Class.Group.Account c310Acc = new SchemaModel.Class.Group.Account();
        c310Acc.setId(LIABILITIES_PERSONAL_ACCOUNT_ID);
        c310Acc.setName(LIABILITIES_PERSONAL_ACCOUNT_NAME);
        c310Acc.setType(Constants.AccountType.LIABILITY);
        c3LiabilitiesGroup.getAccount().add(c310Acc);
        SchemaModel.Class.Group.Account c311Acc = new SchemaModel.Class.Group.Account();
        c311Acc.setId(LIABILITIES_INSTITUTION_ACCOUNT_ID);
        c311Acc.setName(LIABILITIES_INSTITUTION_ACCOUNT_NAME);
        c311Acc.setType(Constants.AccountType.LIABILITY);
        c3LiabilitiesGroup.getAccount().add(c311Acc);
        SchemaModel.Class.Group.Account c312Acc = new SchemaModel.Class.Group.Account();
        c312Acc.setId(LIABILITIES_COMPANY_ACCOUNT_ID);
        c312Acc.setName(LIABILITIES_COMPANY_ACCOUNT_NAME);
        c312Acc.setType(Constants.AccountType.LIABILITY);
        c3LiabilitiesGroup.getAccount().add(c312Acc);
        c3.getGroup().add(c3LiabilitiesGroup);
        newSchemaModel.getClazz().add(c3);

        SchemaModel.Class c4 = new SchemaModel.Class();
        c4.setId("4");
        c4.setName(CLASS_4_NAME);
        SchemaModel.Class.Group c4CapitalGroup = new SchemaModel.Class.Group();
        c4CapitalGroup.setId(CAPITAL_GROUP_ID);
        c4CapitalGroup.setName(CAPITAL_GROUP_NAME);
        SchemaModel.Class.Group.Account c400Acc = new SchemaModel.Class.Group.Account();
        c400Acc.setId(CAPITAL_PERSONAL_ACCOUNT_ID);
        c400Acc.setName(CAPITAL_PERSONAL_ACCOUNT_NAME);
        c400Acc.setType(Constants.AccountType.LIABILITY);
        c4CapitalGroup.getAccount().add(c400Acc);
        c4.getGroup().add(c4CapitalGroup);
        newSchemaModel.getClazz().add(c4);

        SchemaModel.Class c5 = new SchemaModel.Class();
        c5.setId("5");
        c5.setName(CLASS_5_NAME);
        SchemaModel.Class.Group c5DepreciationGroup = new SchemaModel.Class.Group();
        c5DepreciationGroup.setId(DEPRECIATION_GROUP_ID);
        c5DepreciationGroup.setName(DEPRECIATION_GROUP_NAME);
        c5.getGroup().add(c5DepreciationGroup);
        SchemaModel.Class.Group c5ConsumptionGroup = new SchemaModel.Class.Group();
        c5ConsumptionGroup.setId(CONSUMPTION_GROUP_ID);
        c5ConsumptionGroup.setName(CONSUMPTION_GROUP_NAME);
        c5.getGroup().add(c5ConsumptionGroup);
        newSchemaModel.getClazz().add(c5);

        SchemaModel.Class c6 = new SchemaModel.Class();
        c6.setId("6");
        c6.setName(CLASS_6_NAME);
        newSchemaModel.getClazz().add(c6);

        SchemaModel.Class c7 = new SchemaModel.Class();
        c7.setId("7");
        c7.setName(CLASS_7_NAME);
        SchemaModel.Class.Group c7BalanceGroup = new SchemaModel.Class.Group();
        c7BalanceGroup.setId(BALANCE_GROUP_ID);
        c7BalanceGroup.setName(BALANCE_GROUP_NAME);
        SchemaModel.Class.Group.Account c7InitBalanceAcc = new SchemaModel.Class.Group.Account();
        c7InitBalanceAcc.setId(INIT_BALANCE_ACCOUNT_ID);
        c7InitBalanceAcc.setName(INIT_BALANCE_ACCOUNT_NAME);
        c7InitBalanceAcc.setType(Constants.AccountType.OFF_BALANCE);
        c7BalanceGroup.getAccount().add(c7InitBalanceAcc);
        SchemaModel.Class.Group.Account c7ClosingBalanceAcc = new SchemaModel.Class.Group.Account();
        c7ClosingBalanceAcc.setId(CLOSING_BALANCE_ACCOUNT_ID);
        c7ClosingBalanceAcc.setName(CLOSING_BALANCE_ACCOUNT_NAME);
        c7ClosingBalanceAcc.setType(Constants.AccountType.OFF_BALANCE);
        c7BalanceGroup.getAccount().add(c7ClosingBalanceAcc);
        c7.getGroup().add(c7BalanceGroup);
        SchemaModel.Class.Group c7ProfitGroup = new SchemaModel.Class.Group();
        c7ProfitGroup.setId(PROFIT_GROUP_ID);
        c7ProfitGroup.setName(PROFIT_GROUP_NAME);
        SchemaModel.Class.Group.Account c7ProfitStatAcc = new SchemaModel.Class.Group.Account();
        c7ProfitStatAcc.setId(PROFIT_STATEMENT_ACCOUNT_ID);
        c7ProfitStatAcc.setName(PROFIT_STATEMENT_ACCOUNT_NAME);
        c7ProfitStatAcc.setType(Constants.AccountType.OFF_BALANCE);
        c7ProfitGroup.getAccount().add(c7ProfitStatAcc);
        c7.getGroup().add(c7ProfitGroup);
        newSchemaModel.getClazz().add(c7);

        update(newSchemaModel);
        Initializer.LOG.info("File \"" + schemaFileUri + "\" created!");
    }

    @Override
    public SchemaModel retrieve() throws ManagerException {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            javax.xml.validation.Schema xmlSchema = schemaFactory.newSchema(this.getClass().getResource(schemaUri));

            JAXBContext context = JAXBContext.newInstance(SchemaModel.class);

            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setSchema(xmlSchema);

            File file = new File(schemaFileUri);
            return (SchemaModel) unmarshaller.unmarshal(file);
        } catch (Exception e) {
            throw new ManagerException("Error while retrieving schema data: ",e);
        }
    }

    @Override
    public void update(SchemaModel schemaModel) throws ManagerException {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            javax.xml.validation.Schema xmlSchema = schemaFactory.newSchema(this.getClass().getResource(schemaUri));

            JAXBContext context = JAXBContext.newInstance(SchemaModel.class);

            Marshaller marshaller = context.createMarshaller();
            marshaller.setSchema(xmlSchema);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            File file = new File(schemaFileUri);
            marshaller.marshal(schemaModel,new DefaultHandler());
            marshaller.marshal(schemaModel, file);
        } catch (Exception e) {
            throw new ManagerException("Error while updating schema data: ",e);
        }
    }

    @Override
    public void delete() throws ManagerException {
        throw new ManagerException(new NotImplementedException());
    }
}
