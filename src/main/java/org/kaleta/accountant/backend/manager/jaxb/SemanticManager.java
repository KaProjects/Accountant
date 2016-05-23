package org.kaleta.accountant.backend.manager.jaxb;

import org.kaleta.accountant.backend.entity.Semantic;
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
 * Created by Stanislav Kaleta on 21.05.2016.
 */
public class SemanticManager implements Manager<Semantic> {
    private final String schemaUri;
    private final String accFileUri;

    public SemanticManager() {
        schemaUri = "/schema/semantic.xsd";
        accFileUri = Initializer.DATA_SOURCE + "semantic.xml";
    }

    @Override
    public void create() throws ManagerException {
        update(new Semantic());
    }

    @Override
    public Semantic retrieve() throws ManagerException {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            javax.xml.validation.Schema xmlSchema = schemaFactory.newSchema(this.getClass().getResource(schemaUri));

            JAXBContext context = JAXBContext.newInstance(Semantic.class);

            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setSchema(xmlSchema);

            return (Semantic) unmarshaller.unmarshal(new File(accFileUri));
        } catch (Exception e) {
            throw new ManagerException("Error while retrieving semantic data: ",e);
        }
    }

    @Override
    public void update(Semantic semantic) throws ManagerException {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            javax.xml.validation.Schema xmlSchema = schemaFactory.newSchema(this.getClass().getResource(schemaUri));

            JAXBContext context = JAXBContext.newInstance(Semantic.class);

            Marshaller marshaller = context.createMarshaller();
            marshaller.setSchema(xmlSchema);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            marshaller.marshal(semantic, new DefaultHandler());
            marshaller.marshal(semantic, new File(accFileUri));
        } catch (Exception e) {
            throw new ManagerException("Error while updating semantic data: ",e);
        }
    }

    @Override
    public void delete() throws ManagerException {
        throw new ManagerException(new NotImplementedException());
    }
}
