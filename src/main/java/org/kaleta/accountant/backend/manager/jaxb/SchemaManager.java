package org.kaleta.accountant.backend.manager.jaxb;

import org.kaleta.accountant.backend.entity.Schema;
import org.kaleta.accountant.backend.manager.Manager;
import org.kaleta.accountant.backend.manager.ManagerException;
import org.kaleta.accountant.frontend.Initializer;
import org.xml.sax.helpers.DefaultHandler;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.SchemaFactory;
import java.io.File;

/**
 * Created by Stanislav Kaleta on 16.04.2016.
 */
public class SchemaManager implements Manager<Schema> {
    private final String schemaUri;
    private final String schemaFileUri;

    public SchemaManager() {
        schemaUri = "/schema/schema.xsd";
        schemaFileUri = Initializer.DATA_SOURCE + "schema.xml";
    }

    @Override
    public void create() throws ManagerException {
        Schema newSchema = new Schema();
        Schema.Class c0 = new Schema.Class();
        c0.setId("0");
        c0.setName("Long-term Assets");
        newSchema.getClazz().add(c0);
        Schema.Class c1 = new Schema.Class();
        c1.setId("1");
        c1.setName("Current Assets");
        newSchema.getClazz().add(c1);
        Schema.Class c2 = new Schema.Class();
        c2.setId("2");
        c2.setName("Finance");
        newSchema.getClazz().add(c2);
        Schema.Class c3 = new Schema.Class();
        c3.setId("4");
        c3.setName("Relations");
        newSchema.getClazz().add(c3);
        Schema.Class c4 = new Schema.Class();
        c4.setId("4");
        c4.setName("Funding");
        newSchema.getClazz().add(c4);
        Schema.Class c5 = new Schema.Class();
        c5.setId("5");
        c5.setName("Expenses");
        newSchema.getClazz().add(c5);
        Schema.Class c6 = new Schema.Class();
        c6.setId("6");
        c6.setName("Revenues");
        newSchema.getClazz().add(c6);
        Schema.Class c7 = new Schema.Class();
        c7.setId("7");
        c7.setName("Off Balance");
        newSchema.getClazz().add(c7);

        // just pass new instance to update - JAXB is able to create file
        update(newSchema);
    }

    @Override
    public Schema retrieve() throws ManagerException {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            javax.xml.validation.Schema xmlSchema = schemaFactory.newSchema(this.getClass().getResource(schemaUri));

            JAXBContext context = JAXBContext.newInstance(Schema.class);

            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setSchema(xmlSchema);

            File file = new File(schemaFileUri);
            return (Schema) unmarshaller.unmarshal(file);
        } catch (Exception e) {
            throw new ManagerException("Error while retrieving schema data: ",e);
        }
    }

    @Override
    public void update(Schema schema) throws ManagerException {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            javax.xml.validation.Schema xmlSchema = schemaFactory.newSchema(this.getClass().getResource(schemaUri));

            JAXBContext context = JAXBContext.newInstance(Schema.class);

            Marshaller marshaller = context.createMarshaller();
            marshaller.setSchema(xmlSchema);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            File file = new File(schemaFileUri);
            marshaller.marshal(schema,new DefaultHandler());
            marshaller.marshal(schema, file);
        } catch (Exception e) {
            throw new ManagerException("Error while updating schema data: ",e);
        }
    }

    @Override
    public void delete() throws ManagerException {
        throw new ManagerException(new NotImplementedException());
    }
}
