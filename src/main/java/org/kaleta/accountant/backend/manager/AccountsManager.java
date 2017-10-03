package org.kaleta.accountant.backend.manager;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.frontend.Initializer;
import org.xml.sax.helpers.DefaultHandler;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.SchemaFactory;
import java.io.File;

public class AccountsManager implements Manager<AccountsModel> {
    private final String schemaUri;
    private final String schemaFileUri;

    public AccountsManager(String year) {
        schemaUri = "/schema/accounts.xsd";
        schemaFileUri = Initializer.DATA_SOURCE + year + File.separator + "accounts.xml";
    }

    @Override
    public void create() throws ManagerException {
        update(new AccountsModel());
        Initializer.LOG.info("File \"" + schemaFileUri + "\" created!");
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

    @Override
    public void delete() throws ManagerException {
        throw new ManagerException(new NotImplementedException());
    }
}
