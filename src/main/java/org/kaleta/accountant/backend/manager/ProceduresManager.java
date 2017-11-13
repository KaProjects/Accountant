package org.kaleta.accountant.backend.manager;

import org.kaleta.accountant.backend.model.ProceduresModel;
import org.kaleta.accountant.frontend.Initializer;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.SchemaFactory;
import java.io.File;

public class ProceduresManager implements Manager<ProceduresModel>{
    private final String schemaUri;
    private final String schemaFileUri;
    private final String year;

    public ProceduresManager(String year) {
        this.year = year;
        schemaUri = "/schema/procedures.xsd";
        schemaFileUri = Initializer.DATA_SOURCE + year + File.separator + "procedures.xml";
    }

    @Override
    public void create() throws ManagerException {
        ProceduresModel newModel = new ProceduresModel();
        newModel.setYear(year);
        update(newModel);
        Initializer.LOG.info("File created: '" + schemaFileUri + "'");
    }

    @Override
    public ProceduresModel retrieve() throws ManagerException {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            javax.xml.validation.Schema xmlSchema = schemaFactory.newSchema(this.getClass().getResource(schemaUri));

            JAXBContext context = JAXBContext.newInstance(ProceduresModel.class);

            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setSchema(xmlSchema);

            File file = new File(schemaFileUri);
            return (ProceduresModel) unmarshaller.unmarshal(file);
        } catch (Exception e) {
            throw new ManagerException("Error while retrieving procedures data: ",e);
        }
    }

    @Override
    public void update(ProceduresModel proceduresModel) throws ManagerException {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            javax.xml.validation.Schema xmlSchema = schemaFactory.newSchema(this.getClass().getResource(schemaUri));

            JAXBContext context = JAXBContext.newInstance(ProceduresModel.class);

            Marshaller marshaller = context.createMarshaller();
            marshaller.setSchema(xmlSchema);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            File file = new File(schemaFileUri);
            marshaller.marshal(proceduresModel,new DefaultHandler());
            marshaller.marshal(proceduresModel, file);
        } catch (Exception e) {
            throw new ManagerException("Error while updating procedures data: ",e);
        }
    }
}
