package org.kaleta.accountant.backend.manager;

import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.backend.model.ConfigModel;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.SchemaFactory;
import java.io.File;

public class ConfigManager implements Manager<ConfigModel> {
    private final String schemaUri;
    private final String configFileUri;

    public ConfigManager() {
        schemaUri = "/schema/config.xsd";
        configFileUri = Initializer.DATA_SOURCE + "config.xml";
    }

    @Override
    public void create() throws ManagerException {
        ConfigModel configModel = new ConfigModel();
        ConfigModel.Years years = new ConfigModel.Years();
        years.setActive("-1");
        configModel.setYears(years);
        update(configModel);
        Initializer.LOG.info("File created: '" + configFileUri + "'");
    }

    @Override
    public ConfigModel retrieve() throws ManagerException {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            javax.xml.validation.Schema xmlSchema = schemaFactory.newSchema(this.getClass().getResource(schemaUri));

            JAXBContext context = JAXBContext.newInstance(ConfigModel.class);

            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setSchema(xmlSchema);

            return (ConfigModel) unmarshaller.unmarshal(new File(configFileUri));
        } catch (Exception e) {
            throw new ManagerException("Error while retrieving config data: ",e);
        }
    }

    @Override
    public void update(ConfigModel configModel) throws ManagerException {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            javax.xml.validation.Schema xmlSchema = schemaFactory.newSchema(this.getClass().getResource(schemaUri));

            JAXBContext context = JAXBContext.newInstance(ConfigModel.class);

            Marshaller marshaller = context.createMarshaller();
            marshaller.setSchema(xmlSchema);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            File file = new File(configFileUri);
            marshaller.marshal(configModel, new DefaultHandler());
            marshaller.marshal(configModel, file);
        } catch (Exception e) {
            throw new ManagerException("Error while updating config data: ",e);
        }
    }
}
