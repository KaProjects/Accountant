package org.kaleta.accountant.backend.manager.jaxb;

import org.kaleta.accountant.backend.entity.Journal;
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
 * Created by Stanislav Kaleta on 23.05.2016.
 */
public class JournalManager implements Manager<Journal> {
    private final String schemaUri;
    private final String dataSourceDir;

    public JournalManager() {
        schemaUri = "/schema/journal.xsd";
        dataSourceDir = Initializer.DATA_SOURCE + "journal/";
    }

    @Override
    @Deprecated
    public void create() throws ManagerException {
        throw new ManagerException(new UnsupportedOperationException("Use create(int year) method instead."));
    }

    public void create(int year) throws ManagerException {
        Journal journal = new Journal();
        journal.setYear(String.valueOf(year));
        update(journal);
    }

    @Override
    public Journal retrieve() throws ManagerException {
        throw new ManagerException(new UnsupportedOperationException("Use retrieve(int year) method instead."));
    }

    public Journal retrieve(int year) throws ManagerException {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            javax.xml.validation.Schema xmlSchema = schemaFactory.newSchema(this.getClass().getResource(schemaUri));

            JAXBContext context = JAXBContext.newInstance(Journal.class);

            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setSchema(xmlSchema);

            File file = new File(dataSourceDir + "j" + year + ".xml");
            return (Journal) unmarshaller.unmarshal(file);
        } catch (Exception e) {
            throw new ManagerException("Error while retrieving journal data: ",e);
        }
    }

    @Override
    public void update(Journal journal) throws ManagerException {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            javax.xml.validation.Schema xmlSchema = schemaFactory.newSchema(this.getClass().getResource(schemaUri));

            JAXBContext context = JAXBContext.newInstance(Journal.class);

            Marshaller marshaller = context.createMarshaller();
            marshaller.setSchema(xmlSchema);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            File file = new File(dataSourceDir + "j" + journal.getYear() + ".xml");
            marshaller.marshal(journal,new DefaultHandler());
            marshaller.marshal(journal, file);
        } catch (Exception e) {
            throw new ManagerException("Error while updating journal data: ",e);
        }
    }

    @Override
    public void delete() throws ManagerException {
        throw new ManagerException(new NotImplementedException());
    }
}
