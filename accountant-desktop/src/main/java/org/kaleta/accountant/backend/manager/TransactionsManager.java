package org.kaleta.accountant.backend.manager;

import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.backend.model.TransactionsModel;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.SchemaFactory;
import java.io.File;

public class TransactionsManager implements Manager<TransactionsModel> {
    private final String schemaUri;
    private final String schemaFileUri;
    private final String year;

    public TransactionsManager(String year) {
        this.year = year;
        schemaUri = "/schema/transactions.xsd";
        schemaFileUri = Initializer.getDataSource() + year + File.separator + "transactions.xml";
    }

    @Override
    public void create() throws ManagerException {
        TransactionsModel transactionsModel = new TransactionsModel();
        transactionsModel.setYear(year);
        update(transactionsModel);
        Initializer.LOG.info("File created: '" + schemaFileUri + "'");
    }

    @Override
    public TransactionsModel retrieve() throws ManagerException {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            javax.xml.validation.Schema xmlSchema = schemaFactory.newSchema(this.getClass().getResource(schemaUri));

            JAXBContext context = JAXBContext.newInstance(TransactionsModel.class);

            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setSchema(xmlSchema);

            File file = new File(schemaFileUri);
            return (TransactionsModel) unmarshaller.unmarshal(file);
        } catch (Exception e) {
            throw new ManagerException("Error while retrieving transactions data: ",e);
        }
    }

    @Override
    public void update(TransactionsModel transactionsModel) throws ManagerException {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            javax.xml.validation.Schema xmlSchema = schemaFactory.newSchema(this.getClass().getResource(schemaUri));

            JAXBContext context = JAXBContext.newInstance(TransactionsModel.class);

            Marshaller marshaller = context.createMarshaller();
            marshaller.setSchema(xmlSchema);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            File file = new File(schemaFileUri);
            marshaller.marshal(transactionsModel,new DefaultHandler());
            marshaller.marshal(transactionsModel, file);
        } catch (Exception e) {
            throw new ManagerException("Error while updating transactions data: ",e);
        }
    }
}
