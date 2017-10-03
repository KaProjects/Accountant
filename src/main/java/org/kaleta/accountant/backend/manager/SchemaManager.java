package org.kaleta.accountant.backend.manager;

import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Initializer;
import org.xml.sax.helpers.DefaultHandler;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.SchemaFactory;
import java.io.File;

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
        c0.setName("Assets");
        newSchemaModel.getClazz().add(c0);
        SchemaModel.Class c1 = new SchemaModel.Class();
        c1.setId("1");
        c1.setName("Resources");
        newSchemaModel.getClazz().add(c1);
        SchemaModel.Class c2 = new SchemaModel.Class();
        c2.setId("2");
        c2.setName("Finance");
        newSchemaModel.getClazz().add(c2);
        SchemaModel.Class c3 = new SchemaModel.Class();
        c3.setId("4");
        c3.setName("Relations");
        newSchemaModel.getClazz().add(c3);
        SchemaModel.Class c4 = new SchemaModel.Class();
        c4.setId("4");
        c4.setName("Funding");
        newSchemaModel.getClazz().add(c4);
        SchemaModel.Class c5 = new SchemaModel.Class();
        c5.setId("5");
        c5.setName("Expenses");
        newSchemaModel.getClazz().add(c5);
        SchemaModel.Class c6 = new SchemaModel.Class();
        c6.setId("6");
        c6.setName("Revenues");
        newSchemaModel.getClazz().add(c6);
        SchemaModel.Class c7 = new SchemaModel.Class();
        c7.setId("7");
        c7.setName("Off Balance");
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
