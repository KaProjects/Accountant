package org.kaleta.accountant.backend.manager.jaxb;

import org.kaleta.accountant.backend.entity.Procedures;
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
 * Created by Stanislav Kaleta on 24.05.2016.
 */
@Deprecated
public class ProceduresManager implements Manager<Procedures> {
    private final String schemaUri;
    private final String proceduresFileUri;

    public ProceduresManager() {
        schemaUri = "/schema/procedure.xsd";
        proceduresFileUri = Initializer.DATA_SOURCE + "procedures.xml";
    }
    @Override
    public void create() throws ManagerException {
        update(new Procedures());
    }

    @Override
    public Procedures retrieve() throws ManagerException {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            javax.xml.validation.Schema xmlSchema = schemaFactory.newSchema(this.getClass().getResource(schemaUri));

            JAXBContext context = JAXBContext.newInstance(Procedures.class);

            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setSchema(xmlSchema);

            return (Procedures) unmarshaller.unmarshal(new File(proceduresFileUri));
        } catch (Exception e) {
            throw new ManagerException("Error while retrieving procedures data: ",e);
        }
    }

    @Override
    public void update(Procedures procedures) throws ManagerException {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            javax.xml.validation.Schema xmlSchema = schemaFactory.newSchema(this.getClass().getResource(schemaUri));

            JAXBContext context = JAXBContext.newInstance(Procedures.class);

            Marshaller marshaller = context.createMarshaller();
            marshaller.setSchema(xmlSchema);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            File file = new File(proceduresFileUri);
            marshaller.marshal(procedures, new DefaultHandler());
            marshaller.marshal(procedures, file);
        } catch (Exception e) {
            throw new ManagerException("Error while updating semantic data: ",e);
        }
    }

    @Override
    public void delete() throws ManagerException {
        throw new ManagerException(new NotImplementedException());
    }
}
