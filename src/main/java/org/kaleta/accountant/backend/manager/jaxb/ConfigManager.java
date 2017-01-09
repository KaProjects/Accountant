package org.kaleta.accountant.backend.manager.jaxb;

import org.kaleta.accountant.backend.entity.Config;
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
 * Created by Stanislav Kaleta on 19.12.2016.
 */
public class ConfigManager implements Manager<Config> {
    private final String schemaUri;
    private final String ConfigFileUri;

    public ConfigManager() {
        schemaUri = "/schema/config.xsd";
        ConfigFileUri = Initializer.DATA_SOURCE + "config.xml";
    }
    @Override
    public void create() throws ManagerException {
        Config config = new Config();
        Config.Years years = new Config.Years();
        years.setActive("-1");
        config.setYears(years);
        update(config);
    }

    @Override
    public Config retrieve() throws ManagerException {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            javax.xml.validation.Schema xmlSchema = schemaFactory.newSchema(this.getClass().getResource(schemaUri));

            JAXBContext context = JAXBContext.newInstance(Config.class);

            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setSchema(xmlSchema);

            return (Config) unmarshaller.unmarshal(new File(ConfigFileUri));
        } catch (Exception e) {
            throw new ManagerException("Error while retrieving config data: ",e);
        }
    }

    @Override
    public void update(Config config) throws ManagerException {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            javax.xml.validation.Schema xmlSchema = schemaFactory.newSchema(this.getClass().getResource(schemaUri));

            JAXBContext context = JAXBContext.newInstance(Config.class);

            Marshaller marshaller = context.createMarshaller();
            marshaller.setSchema(xmlSchema);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            File file = new File(ConfigFileUri);
            marshaller.marshal(config, new DefaultHandler());
            marshaller.marshal(config, file);
        } catch (Exception e) {
            throw new ManagerException("Error while updating config data: ",e);
        }
    }

    @Override
    public void delete() throws ManagerException {
        throw new ManagerException(new NotImplementedException());
    }
}
