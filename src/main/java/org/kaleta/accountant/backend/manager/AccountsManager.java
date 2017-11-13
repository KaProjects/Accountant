package org.kaleta.accountant.backend.manager;

import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.common.Constants;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.SchemaFactory;
import java.io.File;

public class AccountsManager implements Manager<AccountsModel> {
    private final String schemaUri;
    private final String schemaFileUri;
    private final String year;

    public AccountsManager(String year) {
        this.year = year;
        schemaUri = "/schema/accounts.xsd";
        schemaFileUri = Initializer.DATA_SOURCE + year + File.separator + "accounts.xml";
    }

    @Override
    public void create() throws ManagerException {
        AccountsModel accountsModel = new AccountsModel();
        accountsModel.setYear(year);

        AccountsModel.Account initAcc = new AccountsModel.Account();
        initAcc.setName(Constants.Account.GENERAL_ACCOUNT_NAME);
        initAcc.setSchemaId(Constants.Account.INIT_ACC_ID.split("\\.")[0]);
        initAcc.setSemanticId(Constants.Account.INIT_ACC_ID.split("\\.")[1]);
        accountsModel.getAccount().add(initAcc);

        AccountsModel.Account closingAcc = new AccountsModel.Account();
        closingAcc.setName(Constants.Account.GENERAL_ACCOUNT_NAME);
        closingAcc.setSchemaId(Constants.Account.CLOSING_ACC_ID.split("\\.")[0]);
        closingAcc.setSemanticId(Constants.Account.CLOSING_ACC_ID.split("\\.")[1]);
        accountsModel.getAccount().add(closingAcc);

        AccountsModel.Account profitAcc = new AccountsModel.Account();
        profitAcc.setName(Constants.Account.GENERAL_ACCOUNT_NAME);
        profitAcc.setSchemaId(Constants.Account.PROFIT_ACC_ID.split("\\.")[0]);
        profitAcc.setSemanticId(Constants.Account.PROFIT_ACC_ID.split("\\.")[1]);
        accountsModel.getAccount().add(profitAcc);

        update(accountsModel);
        Initializer.LOG.info("File created: '" + schemaFileUri + "'");
    }

    @Override
    public AccountsModel retrieve() throws ManagerException {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            javax.xml.validation.Schema xmlSchema = schemaFactory.newSchema(this.getClass().getResource(schemaUri));

            JAXBContext context = JAXBContext.newInstance(AccountsModel.class);

            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setSchema(xmlSchema);

            File file = new File(schemaFileUri);
            return (AccountsModel) unmarshaller.unmarshal(file);
        } catch (Exception e) {
            throw new ManagerException("Error while retrieving accounts data: ",e);
        }
    }

    @Override
    public void update(AccountsModel accountsModel) throws ManagerException {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            javax.xml.validation.Schema xmlSchema = schemaFactory.newSchema(this.getClass().getResource(schemaUri));

            JAXBContext context = JAXBContext.newInstance(AccountsModel.class);

            Marshaller marshaller = context.createMarshaller();
            marshaller.setSchema(xmlSchema);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            File file = new File(schemaFileUri);
            marshaller.marshal(accountsModel,new DefaultHandler());
            marshaller.marshal(accountsModel, file);
        } catch (Exception e) {
            throw new ManagerException("Error while updating accounts data: ",e);
        }
    }
}
